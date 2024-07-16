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
package com.crediblebadger.admin;

import com.crediblebadger.storage.StorageService;
import com.crediblebadger.user.User;
import com.crediblebadger.user.UserService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    UserService userService;

    @Autowired
    StorageService storageService;
    
    @GetMapping("/listUsers")
    public List<User> listUsers() {
        return this.userService.list();
    }

    @PostMapping("/suspendUser")    
    public ResponseEntity suspendUser(@RequestBody SuspensionRequestDTO suspensionRequest) {
        boolean result = this.userService.updateSuspensionStatus(suspensionRequest.getUserId(), suspensionRequest.isSuspended());
        
        if (result) {
            return ResponseEntity.ok().build();
        }
        
        return ResponseEntity.badRequest().build();
    }
    
    @GetMapping("/storageInfo")
    public Map<String, Object> retrieveStorageInfo() {
        return this.storageService.retrieveStorageInfo("");
    }
}
