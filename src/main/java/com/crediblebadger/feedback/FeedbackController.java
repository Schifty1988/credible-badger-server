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
package com.crediblebadger.feedback;

import com.crediblebadger.user.security.UserDetailsImpl;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {
    
    @Autowired
    FeedbackService feedbackService;

    @PostMapping("/submit")    
    @CrossOrigin(origins = "*")
    public ResponseEntity submitFeedback(@RequestBody Feedback feedback) {  
        if (!validateFeedback(feedback)) {
            return ResponseEntity.badRequest().build();
        }
        
        boolean result = this.feedbackService.submitFeedback(feedback);
        
        if (result == false) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/retrieve")
    public ResponseEntity<List<Feedback>> retrieveFeedback(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody FeedbackRequest request) {
        
        if (!validateUser(userDetails)) {
            return ResponseEntity.badRequest().build();
        }
        
        String projectKey = String.valueOf(userDetails.getId());
        List<Feedback> feedback = this.feedbackService.retrieveFeedback(projectKey);
        
        if (feedback == null) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok(feedback);
    }
    
    private static boolean validateUser(UserDetailsImpl userDetails) {
        return userDetails != null && 
                userDetails.isEnabled()&& 
                userDetails.getUser().isEmailVerified();     
    }
    
    private static boolean validateFeedback(Feedback feedback) {
        return feedback != null &&
                feedback.getProjectKey() != null &&
                feedback.getContent() != null;
    }
}
