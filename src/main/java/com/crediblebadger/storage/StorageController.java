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

package com.crediblebadger.storage;

import com.crediblebadger.user.security.UserDetailsImpl;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/storage")
public class StorageController {
        private static final int MAX_KEY_LENGTH = 255;
    
    @Autowired
    StorageService storageService;
    
    @GetMapping("/retrieveUserFiles")
    public ResponseEntity<List<String>> retrieveUserFiles(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (!validateUser(userDetails)) {
            return ResponseEntity.badRequest().build();
        }
        
        List<String> retrievedFiles = 
                this.storageService.retrieveUserFiles(userDetails.getId());
        
        return ResponseEntity.ok(retrievedFiles);
    }

    @PostMapping(value="/uploadFile", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)    
    public ResponseEntity uploadFile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("file_name") String fileName, 
            @RequestParam("data") MultipartFile file) throws IOException {
        if (!validateUser(userDetails) || !validateFileName(fileName)) {
            return ResponseEntity.badRequest().build();
        }
        
        boolean result = this.storageService.uploadFile(userDetails.getId(), fileName, file.getBytes());
        
        if (result) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/downloadFile")    
    public ResponseEntity<byte[]> downloadFile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("file_name") String fileName) {
        
        if(!validateUser(userDetails) || !validateFileName(fileName)) {
            return ResponseEntity.badRequest().build();
        }
        
        byte[] data = this.storageService.downloadFile(userDetails.getId(), fileName);              
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"").body(data);
    }

    @PostMapping("/deleteFile")
    public ResponseEntity deleteFile(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("file_name") String fileName) {
        
        if(!validateUser(userDetails) || !validateFileName(fileName)) {
            return ResponseEntity.badRequest().build();
        }
        
        boolean result = this.storageService.deleteFile(userDetails.getId(), fileName);
        
        if (result) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
    
    protected static boolean validateFileName(String fileName) {
        if (fileName == null || fileName.length() > MAX_KEY_LENGTH) {
            return false;
        }
        String regex = "[a-zA-Z0-9._-]+";
        return fileName.matches(regex);
    }
    
    private static boolean validateUser(UserDetailsImpl userDetails) {
        return userDetails != null && 
                userDetails.isEnabled()&& 
                userDetails.getUser().isEmailVerified();     
    }
}
