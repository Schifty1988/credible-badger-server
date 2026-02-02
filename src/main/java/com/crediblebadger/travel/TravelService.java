/*
 *  Copyright © 2024 Michail Ostrowski
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
package com.crediblebadger.travel;

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
public class TravelService {
    @Autowired
    BedrockRuntimeAsyncClient bedrockRuntimeAsyncClient;
    
    @Autowired
    TravelRepository travelRepository;

    private String createCacheKey(String place, boolean isChildFriendly) {
        // removes all whitespaces and commas
        String normalizedPlace = place.replaceAll("[\\s,]", "").toLowerCase();
        return normalizedPlace + "_" + isChildFriendly;
    }
    
    public Flux<TravelRecommendation> createTravelGuideStreaming(String place, boolean isChildFriendly) {
        String searchKey = createCacheKey(place, isChildFriendly);
        TravelGuide cachedGuide = this.travelRepository.retrieveGuide(searchKey);
        if (cachedGuide != null) {
            log.info("Retrieved cached travel guide for searchKey={}", searchKey);
            return Flux.fromIterable(cachedGuide.getTravelRecommendations());
        }
        
        log.info("Creating new travel guide for searchKey={}", searchKey);
        
        TravelGuide travelGuide = new TravelGuide();
        travelGuide.setSearchKey(searchKey);
        travelGuide.setCreationTime(LocalDateTime.now());

        StringBuilder requestBuilder = new StringBuilder();
        requestBuilder.append("{ \"prompt\": \"");
        requestBuilder.append("Create a list with names and very short descriptions of 20 ");
        requestBuilder.append(isChildFriendly ? "child-friendly " : "");
        requestBuilder.append("points of interest in ");
        requestBuilder.append(place);
        requestBuilder.append(" - no additional output.");
        requestBuilder.append("\", \"max_tokens\": \"1000\"}");

        String nativeRequestTemplate =  requestBuilder.toString();

        return streamModelResponse(nativeRequestTemplate, travelGuide);
    }
    
    private void processItem(String currentItem, TravelGuide guide, FluxSink<TravelRecommendation> sink) {
        if (currentItem == null || currentItem.isBlank()) {
            return;
        }

        try {
            TravelRecommendation recommendation = convertStringToRecommendation(currentItem);
            guide.addRecomendation(recommendation);
            sink.next(recommendation);
        }
        catch (Exception e) {
            log.error("Couldn't process item: {}", currentItem, e);
        }
    }
        
    public TravelRecommendation convertStringToRecommendation(String currentValue) {
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
            return new TravelRecommendation();
        } 
        TravelRecommendation currentPointOfInterest = new TravelRecommendation();
        currentPointOfInterest.setName(currentData[0]);
        currentPointOfInterest.setDescription(currentData[1]);
        return currentPointOfInterest;
    }
        
    public Flux<TravelRecommendation> streamModelResponse(String prompt, TravelGuide travelGuide) {
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
        
        Flux<TravelRecommendation> flux = Flux.create(sink -> {
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
                                processItem(bufferContent, travelGuide, sink);
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
                        processItem(bufferContent, travelGuide, sink);
                        sink.complete();
                        this.travelRepository.addTravelGuide(travelGuide);
                        log.info("Stored new TravelGuide={}", travelGuide.getSearchKey());
                    })
                    .onError(e ->  {
                        sink.error(e); 
                        log.error("An error occured while processing response for prompt={}", prompt, e);
                    }).build()
            );
        });
        return flux;
    }
    
    public boolean removeTravelGuide(long travelGuideId) {
        return this.travelRepository.removeTravelGuide(travelGuideId);
    }
}
