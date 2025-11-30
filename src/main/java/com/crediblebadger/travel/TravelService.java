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
import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class TravelService {
    
    @Autowired
    BedrockRuntimeClient bedrockRuntimeClient;
    
    @Autowired
    TravelRepository travelRepository;

    private String createCacheKey(String place, boolean isChildFriendly) {
        // removes all whitespaces and commas
        String normalizedPlace = place.replaceAll("[\\s,]", "").toLowerCase();
        return normalizedPlace + "_" + isChildFriendly;
    }
    
    public TravelGuide createTravelGuide(String place, boolean isChildFriendly) {
        String searchKey = createCacheKey(place, isChildFriendly);
        TravelGuide cachedGuide = this.travelRepository.retrieveGuide(searchKey);
        if (cachedGuide != null) {
            log.info("Retrieved cached travel guide for searchKey={}", searchKey);
            return cachedGuide;
        }
        
        log.info("Creating new travel guide for searchKey={}", searchKey);
        
        try {
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
            requestBuilder.append("\" }");

            String nativeRequestTemplate =  requestBuilder.toString();
            InvokeModelResponse response = this.bedrockRuntimeClient.invokeModel(
                    request -> 
                        request
                        .body(SdkBytes.fromUtf8String(nativeRequestTemplate))
                        .modelId("mistral.mistral-large-2402-v1:0")
            );
            String responseString = response.body().asUtf8String();
            
            ObjectMapper objectMapper = new ObjectMapper();
            MistralResponseWrapper responseWrapper = objectMapper.readValue( responseString, MistralResponseWrapper.class);
            String responseText = responseWrapper.getOutputs()[0].getText();
            log.info("Request={} Response={}", nativeRequestTemplate, responseText);
            
            List<String> pointsOfInterest = extractListFromString(responseText);

            for (String currentValue : pointsOfInterest) {
                String[] currentData = currentValue.split(" - ");
                
                if (currentData.length != 2) {
                    currentData = currentValue.split(": ");
                }
                
                if (currentData.length != 2) {
                    log.error("Couldn't process currentValue={}", currentValue);
                    continue;
                } 
                TravelRecommendation currentPointOfInterest = new TravelRecommendation();
                currentPointOfInterest.setName(currentData[0]);
                currentPointOfInterest.setDescription(currentData[1]);
                travelGuide.addRecomendation(currentPointOfInterest);
            }

            if (travelGuide.getTravelRecommendations().isEmpty()) {
                log.error("createTravelGuide was not able to create recommendations for place={}!", place);
                return null;
            }
            
            this.travelRepository.addTravelGuide(travelGuide);
            return travelGuide;
            
        } catch (JacksonException ex) {
            log.error("createTravelGuide failed for place={}!", place, ex);
            return null;
        }
    }
    
    private List<String> extractListFromString(String input) {
        String trimmedResponseText = input.trim();
        String[] pointsOfInterest = trimmedResponseText.split("\n");

        // sometimes the result is a JSON object
        if (pointsOfInterest.length < 5) {

            boolean stripStart = trimmedResponseText.startsWith("["); 
            boolean stripEnd = trimmedResponseText.endsWith("]");
            int trimmedLength = trimmedResponseText.length();

            trimmedResponseText = trimmedResponseText.substring(
                    stripStart ? 1 : 0, 
                    stripEnd ? trimmedLength - 1 : trimmedLength);
            pointsOfInterest = trimmedResponseText.split(",");
        }
        
        List<String> filteredResult = new LinkedList<>();
        for (String currentString : pointsOfInterest) {
            String processedString = currentString.trim();
            
            if (processedString.isEmpty()) {
                continue;
            }
                     
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

            filteredResult.add(processedString);
        }
        
        return filteredResult;
    }
    
    public boolean removeTravelGuide(long travelGuideId) {
        return this.travelRepository.removeTravelGuide(travelGuideId);
    }
}
