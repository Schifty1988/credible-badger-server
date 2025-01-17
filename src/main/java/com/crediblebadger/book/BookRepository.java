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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Repository;


@Repository
@Transactional
public class BookRepository { 
    @PersistenceContext
    private EntityManager entityManager;
    
    public BookGuide retrieveGuide(String searchKey) {
        TypedQuery<BookGuide> bookGuideQuery = this.entityManager.createNamedQuery(BookGuide.FIND_BOOK_GUIDE_BY_KEY, BookGuide.class);
        bookGuideQuery.setParameter("searchKey", searchKey);
        List<BookGuide> results = bookGuideQuery.getResultList();
        BookGuide result = results.isEmpty() ? null : results.get(0);
        return result;
    }
    
    public void addBookGuide(BookGuide bookGuide) {
        this.entityManager.persist(bookGuide);
    }
    
    public boolean removeBookGuide(long bookGuideId) {
        BookGuide bookGuide = this.entityManager.find(BookGuide.class, bookGuideId);
        
        if (bookGuide == null) {
            return false;
        }
        
        this.entityManager.remove(bookGuide);
        return true;
    }
}
