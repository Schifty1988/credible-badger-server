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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public class NotesRepository {
    @PersistenceContext
    EntityManager entityManager;

    public void addNote(Note note) {
        note.setLastModified(LocalDateTime.now());
        this.entityManager.persist(note);
    }
    
    public void updateNote(Note note) {
        note.setLastModified(LocalDateTime.now());
        this.entityManager.merge(note);
    }
    
    public boolean deleteNote(Note note) {
        Query deleteQuery = this.entityManager.createNamedQuery(Note.DELETE_NOTE);
        deleteQuery.setParameter("id", note.getId());
        deleteQuery.setParameter("userId", note.getUserId());
        int result = deleteQuery.executeUpdate();
        return result == 1;
    }

    public List<Note> retrieveNotesForUser(long userId) {
        TypedQuery<Note> notesForUserQuery = this.entityManager.createNamedQuery(Note.LIST_NOTES_FOR_USER, Note.class);
        notesForUserQuery.setParameter("userId", userId);
        List<Note> results = notesForUserQuery.getResultList();
        return results;
    }   
}
