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
@RequestMapping("/api/recommendation")
@Slf4j
public class RecommendationController {
    
    @Autowired
    RecommendationService recommendationService;
    
    @PostMapping(value = "/travel", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<Recommendation>> createTravelRecommendationStream(
            @AuthenticationPrincipal User user,
            @RequestBody TravelRecommendationRequest request) {
        if (request.getPlace().isBlank() || request.getPlace().length() > 30) {
            return ResponseEntity.badRequest().build();
        }
        String username = user == null ? "Anonymous" : user.getUsername();
        Long userId = user == null ? null : user.getId();
        log.info("{} requested a travel guide for {} ", username, request.getPlace());
        
        Flux<Recommendation> flux = 
                this.recommendationService.streamTravelRecommendations(userId, request.getPlace(), request.isChildFriendly());
        return ResponseEntity
            .ok()
            .header("X-Accel-Buffering", "no") // disable nginx buffering
            .body(flux);
    }
    
    @PostMapping(value = "/movie", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<Recommendation>> createMovieRecommendationStream(
            @AuthenticationPrincipal User user,
            @RequestBody MovieRecommendationRequest request) {
        if (request.getName().isBlank() || request.getName().length() > 30) {
            return ResponseEntity.badRequest().build();
        }
        String username = user == null ? "Anonymous" : user.getUsername();
        Long userId = user == null ? null : user.getId();
        log.info("{} requested a movie guide for {} ", username, request.getName());
        
        Flux<Recommendation> flux = 
                this.recommendationService.streamMovieRecommendations(userId, request.getName());
        return ResponseEntity
            .ok()
            .header("X-Accel-Buffering", "no") // disable nginx buffering
            .body(flux);
    }
    
    @PostMapping(value = "/book", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<Flux<Recommendation>> createBookRecommendationStream(
            @AuthenticationPrincipal User user,
            @RequestBody BookRecommendationRequest request) {
        if (request.getName().isBlank() || request.getName().length() > 30) {
            return ResponseEntity.badRequest().build();
        }
        String username = user == null ? "Anonymous" : user.getUsername();
        Long userId = user == null ? null : user.getId();
        log.info("{} requested a book guide for {} ", username, request.getName());
        
        Flux<Recommendation> flux = 
                this.recommendationService.streamBookRecommendations(userId, request.getName());
        return ResponseEntity
            .ok()
            .header("X-Accel-Buffering", "no") // disable nginx buffering
            .body(flux);
    }
    
    @PostMapping(value = "/like")
    public ResponseEntity<Void> likeRecommendation(
            @AuthenticationPrincipal User user,
            @RequestBody RecommendationLikeRequest request) {
        this.recommendationService.likeRecommendation(user.getId(), request.getRecommendationId());
        return ResponseEntity.ok().build();
    }
    
    @PostMapping(value = "/unlike")
    public ResponseEntity<Void> unlikeRecommendation(
            @AuthenticationPrincipal User user,
            @RequestBody RecommendationLikeRequest request) {
        this.recommendationService.unlikeRecommendation(user.getId(), request.getRecommendationId());
        return ResponseEntity.ok().build();
    }
}
