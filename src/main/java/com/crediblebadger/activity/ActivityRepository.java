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
package com.crediblebadger.activity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class ActivityRepository {
    
    @PersistenceContext
    private EntityManager entityManager;

    public void storeActivity(Activity activity) {
        activity.setCreationTime(LocalDateTime.now());
        this.entityManager.persist(activity);
    }
    
    public boolean deleteActivity(Activity activity) {
        Query deleteQuery = this.entityManager.createNamedQuery(Activity.DELETE_ACTIVITY);
        deleteQuery.setParameter("id", activity.getId());
        deleteQuery.setParameter("userId", activity.getUserId());
        int result = deleteQuery.executeUpdate();
        return result == 1;
    }

    List<Activity> retrieveActivities(long userId) {
        TypedQuery<Activity> userQuery = this.entityManager.createNamedQuery(Activity.FIND_ACTIVITY_BY_USER_ID, Activity.class);
        userQuery.setParameter("userId", userId);
        List<Activity> results = userQuery.getResultList();
        return results;
    }
}
