/*
 *  Copyright © 2026 Michail Ostrowski
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
package com.crediblebadger.notes;

import com.crediblebadger.user.User;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/notes")
public class NotesController {
    @Autowired
    NotesService notesService;

    @PostMapping("/submit")
    public ResponseEntity addNote(
            @AuthenticationPrincipal User user, 
            @RequestBody Note note) {
        note.setUserId(user.getId());
        this.notesService.addNote(note);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/update")
    public ResponseEntity updateNote(
            @AuthenticationPrincipal User user, 
            @RequestBody Note note) {
        
        if (!user.getId().equals(note.getUserId())) {
            return ResponseEntity.badRequest().build();
        }
        
        this.notesService.update(note);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/delete")
    public ResponseEntity deleteNote(
            @AuthenticationPrincipal User user, 
            @RequestBody Note note) {
        note.setUserId(user.getId());
        this.notesService.deleteNote(note);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/retrieve")
    public ResponseEntity<List<Note>> retrieveNotes(@AuthenticationPrincipal User user) {
        List<Note> result = this.notesService.retrieveNotes(user.getId());
        return ResponseEntity.ok(result);
    }
}
