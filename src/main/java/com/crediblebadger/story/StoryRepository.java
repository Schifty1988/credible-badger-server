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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public class StoryRepository {
    
    @PersistenceContext
    private EntityManager entityManager;

    public Story retrieveStory(long storyId) {   
        TypedQuery<Story> userQuery = this.entityManager.createNamedQuery(Story.FIND_STORY_BY_ID, Story.class);
        userQuery.setParameter("id", storyId);
        List<Story> results = userQuery.getResultList();
        Story result = results.isEmpty() ? null : results.get(0);
        return result;
    } 

    public boolean submitStory(Story story) {
        this.entityManager.persist(story);
        return true;
    }

    public Story retrieveFirstStory() {
        TypedQuery<Story> userQuery = this.entityManager.createNamedQuery(Story.FIND_FIRST_STORY, Story.class);
        List<Story> results = userQuery.getResultList();
        Story result = results.isEmpty() ? null : results.get(0);
        return result;
    }
}
