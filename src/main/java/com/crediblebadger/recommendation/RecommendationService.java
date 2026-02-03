/*
 *  Copyright © 2026 Michail Ostrowski
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.crediblebadger.recommendation;

import com.crediblebadger.ai.mistral.MistralRequest;
import com.crediblebadger.ai.mistral.MistralResponseWrapper;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithResponseStreamRequest;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelWithResponseStreamResponseHandler;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class RecommendationService {

    @Autowired
    BedrockRuntimeAsyncClient bedrockRuntimeAsyncClient;
    
    @Autowired
    RecommendationRepository recommendationRepository;

    private String createTravelGroupKey(String place, boolean isChildFriendly) {
        // removes all whitespaces and commas
        String normalizedPlace = place.replaceAll("[\\s,]", "").toLowerCase();
        return normalizedPlace + "_" + isChildFriendly;
    }
    
    private String createMovieGroupKey(String name) {
        // removes all whitespaces, commas, colons, and 'the '
        String normalizedName = name.replaceAll("^(?i)the\\s+|[\\s,:&!’-]", "").toLowerCase();
        return normalizedName;
    }
    
    private String createBookGroupKey(String name) {
        // removes all whitespaces, commas, colons, and 'the '
        String normalizedName = name.replaceAll("^(?i)the\\s+|[\\s,:&!’-]", "").toLowerCase();
        return normalizedName;
    }
    
    public Flux<Recommendation> streamTravelRecommendations(String place, boolean isChildFriendly) {
        String searchKey = createTravelGroupKey(place, isChildFriendly);
        
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Create a list with names and very short descriptions of 20 ");
        promptBuilder.append(isChildFriendly ? "child-friendly " : "");
        promptBuilder.append("points of interest in ");
        promptBuilder.append(place);
        promptBuilder.append(" - no additional output.");
        
        return streamRecommendations(searchKey, promptBuilder.toString(), RecommendationType.PLACE);
    }
    
    public Flux<Recommendation> streamMovieRecommendations(String name) {
        String searchKey = createMovieGroupKey(name);
        
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Create a list with names and very short descriptions of 20 movies similar to ");
        promptBuilder.append(name);
        promptBuilder.append(" - no additional output.");
        
        return streamRecommendations(searchKey, promptBuilder.toString(), RecommendationType.MOVIE);
    }
    
    public Flux<Recommendation> streamBookRecommendations(String name) {
        String searchKey = createBookGroupKey(name);
        
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Create a list with names and very short descriptions of 20 books similar to ");
        promptBuilder.append(name);
        promptBuilder.append(" - no additional output.");
        
        return streamRecommendations(searchKey, promptBuilder.toString(), RecommendationType.BOOK);
    }
        
    private Flux<Recommendation> streamRecommendations(String searchKey, String prompt, RecommendationType type) {
        RecommendationGroup cachedGroup = 
                this.recommendationRepository.retrieveRecommendationGroup(searchKey, type);
        
        if (cachedGroup != null) {
            log.info("Retrieved cached recommendations for searchKey={} and type={}", searchKey, type);
            return Flux.fromIterable(cachedGroup.getRecommendations());
        }
        
        log.info("Creating new recommendations for searchKey={} and type={}", searchKey, type);
        
        RecommendationGroup recommendationGroup = new RecommendationGroup();
        recommendationGroup.setType(type);
        recommendationGroup.setSearchKey(searchKey);
        recommendationGroup.setCreationTime(LocalDateTime.now());
        
        MistralRequest request = new MistralRequest();
        request.setPrompt(prompt);
        request.setMax_tokens(1000);
        String nativeRequestTemplate =  new ObjectMapper().writeValueAsString(request);

        return streamModelResponse(nativeRequestTemplate, recommendationGroup);
    }
    
    private Flux<Recommendation> streamModelResponse(String prompt, RecommendationGroup recommendationGroup) {
        InvokeModelWithResponseStreamRequest request =
        InvokeModelWithResponseStreamRequest.builder()
                .modelId("mistral.mistral-large-2402-v1:0")
                .body(SdkBytes.fromUtf8String(prompt))
                .build();
        
        // sometimes the result is a JSON object
//        if (pointsOfInterest.length < 5) {
//
//            boolean stripStart = trimmedResponseText.startsWith("["); 
//            boolean stripEnd = trimmedResponseText.endsWith("]");
//            int trimmedLength = trimmedResponseText.length();
//
//            trimmedResponseText = trimmedResponseText.substring(
//                    stripStart ? 1 : 0, 
//                    stripEnd ? trimmedLength - 1 : trimmedLength);
//            pointsOfInterest = trimmedResponseText.split(",");
//        }
        
        Flux<Recommendation> flux = Flux.create(sink -> {
            StringBuilder buffer = new StringBuilder();
            ObjectMapper objectMapper = new ObjectMapper();
                        
            this.bedrockRuntimeAsyncClient.invokeModelWithResponseStream(request,
                InvokeModelWithResponseStreamResponseHandler.builder()
                    .onEventStream(stream -> {
                        stream.subscribe(event -> {
                            event.accept(InvokeModelWithResponseStreamResponseHandler.Visitor.builder()
                                .onChunk(chunk -> {

                            String json = chunk.bytes().asUtf8String();                            
                            
                            MistralResponseWrapper responseWrapper = objectMapper.readValue( json, MistralResponseWrapper.class);
                            String responseText = responseWrapper.getOutputs()[0].getText();
                            String stop_reason = responseWrapper.getOutputs()[0].getStop_reason();
                            
                            if (stop_reason != null && !stop_reason.equals("stop")) {
                                log.error("An unexpected stop_reason={} occured for promt={}", stop_reason, prompt);
                            }
                            
                            buffer.append(responseText); 
                            String bufferContent = buffer.toString();

                            String[] lines = bufferContent.split("\n", -1);

                            for (int i = 0; i < lines.length - 1; i++) {  
                                processItem(bufferContent, recommendationGroup, sink);
                            }

                            buffer.setLength(0);
                            buffer.append(lines[lines.length - 1]);
                            })
                            .onDefault(e -> {
                                // Handle other event types if needed
                            }).build());
                        });
                    })
                    .onComplete(() -> {
                        String bufferContent = buffer.toString();
                        processItem(bufferContent, recommendationGroup, sink);
                        sink.complete();
                        this.recommendationRepository.addRecommendationGroup(recommendationGroup);
                        log.info("Stored new RecommendationGroup={}", recommendationGroup.getSearchKey());
                    })
                    .onError(e ->  {
                        sink.error(e); 
                        log.error("An error occured while processing response for prompt={}", prompt, e);
                    }).build()
            );
        });
        return flux;
    }
    
    private void processItem(String currentItem, RecommendationGroup group, FluxSink<Recommendation> sink) {
        if (currentItem == null || currentItem.isBlank()) {
            return;
        }

        try {
            Recommendation recommendation = convertStringToRecommendation(currentItem);
            
            if (recommendation != null) {
                group.addRecommendation(recommendation);
                sink.next(recommendation);
            }
        }
        catch (Exception e) {
            log.error("Couldn't process item: {}", currentItem, e);
        }
    }
        
    private Recommendation convertStringToRecommendation(String currentValue) {
         String processedString = currentValue.trim();

        // sometimes line is wrapped in ""
        // sometimes word is highlighted with **
        processedString = processedString.replace("\"", "").replace("**", "");

        int firstSpace = processedString.indexOf(" ");
        int firstCharacter = processedString.charAt(0);

        // assuming list format:  33. Zoo
        if (Character.isDigit(firstCharacter) && firstSpace != -1) {
            processedString = processedString.substring(firstSpace + 1);    
        }

        // assuming list format: - Zoo
        if (processedString.startsWith("- ")) {
            processedString = processedString.substring(2);
        }

        String[] currentData = processedString.split(" - ");

        if (currentData.length != 2) {
            currentData = processedString.split(": ");
        }

        if (currentData.length != 2) {
            log.error("Couldn't process currentValue={}", processedString);
            return null;
        } 
        Recommendation recommendation = new Recommendation();
        recommendation.setName(currentData[0]);
        recommendation.setDescription(currentData[1]);
        return recommendation;
    }
    
    public boolean removeRecommendationGroup(long recommendationGroupId) {
        return this.recommendationRepository.removeRecommendationGroup(recommendationGroupId);
    }
}
