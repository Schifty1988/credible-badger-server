/*
 *  Copyright © 2025 Michail Ostrowski
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

import com.crediblebadger.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StoryService {
    @Autowired
    StoryRepository storyRepository; 
    
    @Autowired
    StorageService storageService;
    
    public Story retrieveFirstStory() {
        Story story = this.storyRepository.retrieveFirstStory();
        processStory(story);
        return story;
    }

    public Story retrieveStory(long storyId) {
        Story story = this.storyRepository.retrieveStory(storyId);
        processStory(story);
        return story;
    }

    public boolean submitStory(Story story) {
        boolean result = this.storyRepository.submitStory(story);
        return result;
    }
    
    private void processStory(Story story) {
        String keyPrefix = "stories/" + story.getId() + "/";
        String musicURL = this.storageService.generateResourceURL(keyPrefix + story.getMusic());
        story.setMusic(musicURL);
        
        for (StoryPart currentPart : story.getParts()) {
            String audioURL = this.storageService.generateResourceURL(keyPrefix + currentPart.getAudio());
            String imageURL = this.storageService.generateResourceURL(keyPrefix + currentPart.getImage());
            
            currentPart.setAudio(audioURL);
            currentPart.setImage(imageURL);
        }
    }
}
