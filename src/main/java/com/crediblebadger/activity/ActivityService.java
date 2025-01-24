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
package com.crediblebadger.activity;

import jakarta.transaction.Transactional;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
@Slf4j
public class ActivityService {
    
    @Autowired
    ActivityRepository activityRepository;
    
    public boolean submitActivity(Activity activity) {    
        log.info("Storing activity for user={}", activity.getUserId());
        this.activityRepository.storeActivity(activity);
        return true;
    }
    
    public boolean deleteActivity(Activity activity) {    
        log.info("Deleting activity for user={}", activity.getUserId());
        return this.activityRepository.deleteActivity(activity);
    }
    
    public List<Activity> retrieveActivities(long userId) {
        List<Activity> results = this.activityRepository.retrieveActivities(userId);
        return results;
    }    
}
