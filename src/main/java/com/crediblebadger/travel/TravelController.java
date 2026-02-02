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

import com.crediblebadger.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/travel")
@Slf4j
public class TravelController {
    
    @Autowired
    TravelService travelService;
    
    @PostMapping(value = "/travelGuide", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<TravelRecommendation>> createTravelRecommendationStream(
            @AuthenticationPrincipal User user,
            @RequestBody TravelGuideRequest request) {
        if (request.getPlace().isBlank() || request.getPlace().length() > 30) {
            return ResponseEntity.badRequest().build();
        }
        String username = user == null ? "Anonymous" : user.getUsername();
        log.info("{} requested a travel guide for {} ", username, request.getPlace());
        
        Flux<TravelRecommendation> flux = 
                this.travelService.createTravelGuideStreaming(request.getPlace(), request.isChildFriendly());
        return ResponseEntity
            .ok()
            .header("X-Accel-Buffering", "no") // disable nginx buffering
            .body(flux);
    }
}
