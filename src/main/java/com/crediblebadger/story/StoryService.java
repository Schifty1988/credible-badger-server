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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StoryService {
    @Autowired
    StoryRepository storyRepository; 
    
    public Story retrieveFirstStory() {
        return this.storyRepository.retrieveFirstStory();
    }

    public Story retrieveStory(long storyId) {
        Story story = this.storyRepository.retrieveStory(storyId);
        return story;
    }

    public boolean submitStory(Story story) {
        boolean result = this.storyRepository.submitStory(story);
        return result;
    }
}