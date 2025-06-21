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

import com.crediblebadger.user.User;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activity")
public class ActivityController {
    
    @Autowired
    ActivityService activityService;

    @PostMapping("/submit")    
    public ResponseEntity submitActivity(
            @AuthenticationPrincipal User user,
            @RequestBody Activity activity) {  
        if (!validateActivity(activity)) {
            return ResponseEntity.badRequest().build();
        }
        
        activity.setUserId(user.getId());
        boolean result = this.activityService.submitActivity(activity);
        
        if (result == false) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/delete")    
    public ResponseEntity deleteActivity(
            @AuthenticationPrincipal User user,
            @RequestBody Activity activity) {  
        if (!validateActivity(activity)) {
            return ResponseEntity.badRequest().build();
        }
        
        activity.setUserId(user.getId());
        boolean result = this.activityService.deleteActivity(activity);
        
        if (result == false) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok().build();
    }
    
    
    @PostMapping("/retrieve")
    public ResponseEntity<List<Activity>> retrieveActivity(
            @AuthenticationPrincipal User user,
            @RequestBody ActivityRequest request) {
        
        if (!User.validateUser(user)) {
            return ResponseEntity.badRequest().build();
        }

        List<Activity> activities = this.activityService.retrieveActivities(user.getId());
        
        if (activities == null) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok(activities);
    }
    
    private static boolean validateActivity(Activity activity) {
        return activity != null &&
                activity.getName() != null &&
                activity.getName().isBlank() == false;
    }
}
