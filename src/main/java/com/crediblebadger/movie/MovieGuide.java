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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import lombok.Data;

@Data
@Entity
@NamedQuery(name = MovieGuide.FIND_MOVIE_GUIDE_BY_KEY, query = "From MovieGuide where searchKey = :searchKey")
public class MovieGuide {
    public static final String FIND_MOVIE_GUIDE_BY_KEY = "MovieGuide_FindMovieGuideBySearchKey";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id = 0l;
    
    @Version
    private Long version;
    
    @Column
    String searchKey;
    
    @Column
    LocalDateTime creationTime;
    
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST, orphanRemoval = true)
    @JoinColumn(name="movieGuideId")
    private List<MovieRecommendation> movieRecommendations = new LinkedList<>();
    
    public void addRecomendation(MovieRecommendation movieRecommendation) {
        movieRecommendation.setMovieGuide(this);
        this.movieRecommendations.add(movieRecommendation);
    }
}
