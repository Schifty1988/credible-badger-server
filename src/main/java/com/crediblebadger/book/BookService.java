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
package com.crediblebadger.book;

import com.crediblebadger.ai.mistral.MistralResponseWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.InvokeModelResponse;

@Service
@Slf4j
public class BookService {
    
    @Autowired
    BedrockRuntimeClient bedrockRuntimeClient;
    
    @Autowired
    BookRepository bookRepository;

    private String createCacheKey(String name) {
        // removes all whitespaces, commas, colons, and 'the '
        String normalizedName = name.replaceAll("^(?i)the\\s+|[\\s,:&!’-]", "").toLowerCase();
        return normalizedName;
    }
    
    public BookGuide createBookGuide(String name) {
        String searchKey = createCacheKey(name);
        
        BookGuide cachedGuide = this.bookRepository.retrieveGuide(searchKey);
        if (cachedGuide != null) {
            log.info("Retrieved cached book guide for searchKey={}", searchKey);
            return cachedGuide;
        }
        
        log.info("Creating new book guide for searchKey={}", searchKey);
        
        try {
            BookGuide bookGuide = new BookGuide();
            bookGuide.setSearchKey(searchKey);
            bookGuide.setCreationTime(LocalDateTime.now());

            StringBuilder requestBuilder = new StringBuilder();
            requestBuilder.append("{ \"max_tokens\": 1024, \"prompt\": \"");
            requestBuilder.append("Create a list with names and very short descriptions of 20 books similar to ");
            requestBuilder.append(name);
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
                BookRecommendation currentPointOfInterest = new BookRecommendation();
                currentPointOfInterest.setName(currentData[0]);
                
                String shortDescription = shortenDescription(currentData[1], 200);
                currentPointOfInterest.setDescription(shortDescription);
                if (currentData[0].length() < 100 && shortDescription.length() > 1) {
                    bookGuide.addRecomendation(currentPointOfInterest);   
                }
            }

            if (bookGuide.getBookRecommendations().isEmpty()) {
                log.error("createBookGuide was not able to create recommendations for book={}!", name);
                return null;
            }
            this.bookRepository.addBookGuide(bookGuide); 
            return bookGuide;
            
        } catch (JsonProcessingException ex) {
            log.error("createBookGuide failed for name={}!", name, ex);
            return null;
        }
    }
    
    private String shortenDescription(String description, int maxLength) {
        if (description.length() < maxLength) {
            return description;
        }
        
        int periodIndex = 0;
        int nextPeriodIndex = 0;
        
        while (nextPeriodIndex != -1 && nextPeriodIndex < maxLength) {
            periodIndex = nextPeriodIndex;
            nextPeriodIndex = description.indexOf(".", periodIndex + 1);
        }
        
        log.error("Description={} is too long - shortening!", description);
        String shortDesc = description.substring(0, periodIndex + 1);
        
        return shortDesc;
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
}
