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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/travel")
public class TravelController {
    
    @Autowired
    TravelService travelService;
    
    @PostMapping("/travelGuide")
    public ResponseEntity<TravelGuide> createTravelGuide(@RequestBody TravelGuideRequest request) {
        if (request.getPlace().isBlank() || request.getPlace().length() > 30) {
            return ResponseEntity.badRequest().build();
        }
        
        TravelGuide travelGuide = this.travelService.createTravelGuide(request.getPlace(), request.isChildFriendly());
        
        if (travelGuide == null) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok(travelGuide);
    }
    
}
