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
package com.crediblebadger.story;

import com.crediblebadger.user.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/story")
public class StoryController {
    
    @Autowired
    StoryService storyService;
    
    @PostMapping("/submit")
    public ResponseEntity submitStory(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody Story story) {
        
        if (userDetails == null || userDetails.isEnabled() == false) {
            return ResponseEntity.badRequest().build();
        }
        
        story.initStoryParts();
        story.setUserId(userDetails.getUser().getId());
        boolean result = this.storyService.submitStory(story);
        
        if (!result) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/retrieve")
    public ResponseEntity<Story> retrieveFirstStory() {
        Story story = this.storyService.retrieveFirstStory();
        return ResponseEntity.ok(story);
    } 
    
    @GetMapping("/retrieve/{storyId}")
    public ResponseEntity<Story> retrieveStory(@PathVariable long storyId) {
        Story story = this.storyService.retrieveStory(storyId);
        return ResponseEntity.ok(story);
    } 
}
