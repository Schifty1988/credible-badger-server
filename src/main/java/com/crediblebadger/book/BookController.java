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
package com.crediblebadger.book;

import com.crediblebadger.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/book")
@Slf4j
public class BookController {
    
    @Autowired
    BookService bookService;
    
    @PostMapping("/bookGuide")
    public ResponseEntity<BookGuide> createBookGuide(
            @AuthenticationPrincipal User user,
            @RequestBody BookGuideRequest request) {
        if (request.getName().isBlank() || request.getName().length() > 60) {
            return ResponseEntity.badRequest().build();
        }
        String username = user == null ? "Anonymous" : user.getUsername();
        log.info("{} requested a book guide for {} ", username, request.getName());
        BookGuide bookGuide = this.bookService.createBookGuide(request.getName());
        
        if (bookGuide == null) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok(bookGuide);
    }
    
}
