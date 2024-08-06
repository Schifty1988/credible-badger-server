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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
@Slf4j
public class StorageService {  
    private final S3Client s3Client;
    
    @Value("${app.storage.bucket}")
    String storageBucket;
    
    @Value("${app.storage.user-limit-mb}")
    int userLimit;

    public StorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    private String createObjectKey(long userId, String fileName) {
        String userPrefix = createUserPrefix(userId);
        String objectKey = userPrefix + fileName;
        return objectKey;
    }
    
    private String createUserPrefix(long userId) {
        String userPrefix = "users/" + userId + "/";
        return userPrefix;
    }

    public List<String> retrieveUserFiles(long userId) {
        List<String> userFiles = new LinkedList<>();
        String userPrefix = createUserPrefix(userId);
        int prefixLength = userPrefix.length();

        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(this.storageBucket)
                .maxKeys(1)
                .prefix(userPrefix)
                .build();

        boolean hasMoreData = true;
        
        while (hasMoreData) {
            ListObjectsV2Response listResponse = 
                    this.s3Client.listObjectsV2(listRequest);
            
            for (S3Object currentContent : listResponse.contents()) {
                userFiles.add(currentContent.key().substring(prefixLength));
            }
            hasMoreData = listResponse.nextContinuationToken() != null;

            listRequest = listRequest.toBuilder()
                    .continuationToken(listResponse.nextContinuationToken())
                    .build();
        }
        log.info("Retrieved {} files for userId {}", userFiles.size(), userId);
        return userFiles;
    }
    
    public boolean checkUserUploadLimit(long userId, byte[] data) {
        String userPrefix = createUserPrefix(userId);
        Map<String, Object> userData = retrieveStorageInfo(userPrefix);

        long requestedStorage = (Long) userData.get("totalSize") + data.length;
        long requestedStorageMB = requestedStorage / (1024 * 1024);
        return requestedStorageMB < this.userLimit;  
    }
    
    public boolean uploadFile(long userId, String fileName, byte[] data) {
        if (!checkUserUploadLimit(userId, data)) {
            log.info("Requested upload denied: storage exceeds limits!");
            return false;
        }
        
        String objectKey = createObjectKey(userId, fileName);
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(this.storageBucket)
                .key(objectKey)
                .build();
        PutObjectResponse putResponse = 
                this.s3Client.putObject(putRequest, RequestBody.fromBytes(data));
        log.info("Uploaded {} with response {}", objectKey, putResponse);
        return true;
    }
    
    public byte[] downloadFile(long userId, String fileName) {
        String objectKey = createObjectKey(userId, fileName);
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(this.storageBucket)
                .key(objectKey)
                .build();
        
        ResponseBytes<GetObjectResponse> getResponse = 
                this.s3Client.getObjectAsBytes(getRequest);
        log.info("Downloaded {} successfully!", objectKey);
        byte[] data = getResponse.asByteArray();
        return data;
    }
    
    public boolean deleteFile(long userId, String fileName) {
        String objectKey = createObjectKey(userId, fileName);
        
        DeleteObjectRequest deleteRequest = 
                DeleteObjectRequest.builder()
                        .bucket(this.storageBucket)
                        .key(objectKey)
                        .build();    
        DeleteObjectResponse deleteResponse = 
                this.s3Client.deleteObject(deleteRequest);  
        log.info("Deleted {} for user {} with response {}", fileName, userId, deleteResponse);
        return true;
    }    

    public Map<String, Object> retrieveStorageInfo(String prefix) {
        Long totalSize = 0l;
        Long numFiles = 0l;

        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(this.storageBucket)
                .maxKeys(1)
                .prefix(prefix)
                .build();

        boolean hasMoreData = true;
        
        while (hasMoreData) {
            ListObjectsV2Response listResponse = 
                    this.s3Client.listObjectsV2(listRequest);
            
            for (S3Object currentContent : listResponse.contents()) {
                totalSize += currentContent.size();
                ++numFiles;
            }
            hasMoreData = listResponse.nextContinuationToken() != null;

            listRequest = listRequest.toBuilder()
                    .continuationToken(listResponse.nextContinuationToken())
                    .build();
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("bucket", this.storageBucket);
        result.put("numberOfFiles", numFiles);
        result.put("totalSize", totalSize);
        log.info("Retrieved {} files with a total size of {}", numFiles, totalSize);
        return result;
    }
}
