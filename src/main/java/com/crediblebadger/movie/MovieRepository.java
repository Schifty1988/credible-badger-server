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
package com.crediblebadger.movie;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Repository;


@Repository
@Transactional
public class MovieRepository { 
    @PersistenceContext
    private EntityManager entityManager;
    
    public MovieGuide retrieveGuide(String searchKey) {
        TypedQuery<MovieGuide> movieGuideQuery = this.entityManager.createNamedQuery(MovieGuide.FIND_MOVIE_GUIDE_BY_KEY, MovieGuide.class);
        movieGuideQuery.setParameter("searchKey", searchKey);
        List<MovieGuide> results = movieGuideQuery.getResultList();
        MovieGuide result = results.isEmpty() ? null : results.get(0);
        return result;
    }
    
    public void addMovieGuide(MovieGuide movieGuide) {
        this.entityManager.persist(movieGuide);
    }
    
    public boolean removeMovieGuide(long movieGuideId) {
        MovieGuide movieGuide = this.entityManager.find(MovieGuide.class, movieGuideId);
        
        if (movieGuide == null) {
            return false;
        }
        
        this.entityManager.remove(movieGuide);
        return true;
    }
}
