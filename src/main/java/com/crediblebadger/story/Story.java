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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Version;
import java.util.LinkedList;
import java.util.List;
import lombok.Data;

@Data
@Entity
@NamedQuery(name = Story.FIND_STORY_BY_ID, query = "From Story where id = :id")
@NamedQuery(name = Story.FIND_FIRST_STORY, query = "From Story ORDER BY id ASC LIMIT 1")
public class Story {
    public static final String FIND_STORY_BY_ID = "Story_FindStoryById";
    public static final String FIND_FIRST_STORY = "Story_FindFirstStory";
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id = null;
    @Column
    private long userId;
    @Column
    private String title;
    @Column
    private String music;
    @Version
    private long version;
    
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST, orphanRemoval = true)
    @JoinColumn(name="story_id")
    @OrderBy("part ASC")
    private List<StoryPart> parts = new LinkedList();
    
    public void addStoryPart(StoryPart storyPart) {
        storyPart.setPart(this.parts.size());
        storyPart.setStory(this);
        this.parts.add(storyPart);
    }
    
    public void initStoryParts() {       
        for (int i = 0; i < this.parts.size(); ++i) {
            StoryPart currentStoryPart = this.parts.get(i);
            currentStoryPart.setStory(this);
            currentStoryPart.setPart(i);
        }
    }  
}
