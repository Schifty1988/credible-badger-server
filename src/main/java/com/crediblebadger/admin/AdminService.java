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

import com.crediblebadger.book.BookGuide;
import com.crediblebadger.book.BookRecommendation;
import com.crediblebadger.book.BookRepository;
import com.crediblebadger.movie.MovieGuide;
import com.crediblebadger.movie.MovieRecommendation;
import com.crediblebadger.movie.MovieRepository;
import com.crediblebadger.recommendation.Recommendation;
import com.crediblebadger.recommendation.RecommendationGroup;
import com.crediblebadger.recommendation.RecommendationRepository;
import com.crediblebadger.recommendation.RecommendationType;
import com.crediblebadger.travel.TravelGuide;
import com.crediblebadger.travel.TravelRecommendation;
import com.crediblebadger.travel.TravelRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AdminService {
    @Autowired
    TravelRepository travelRepository;
    
    @Autowired
    BookRepository bookRepository;
    
    @Autowired
    MovieRepository movieRepository;

    @Autowired
    RecommendationRepository recommendationRepository;
    
    void migrate() {
        List<TravelGuide> travelGuides = this.travelRepository.getAllGuides();
        List<BookGuide> bookGuides = this.bookRepository.getAllGuides();
        List<MovieGuide> movieGuides = this.movieRepository.getAllGuides();
        
        int total = travelGuides.size() + bookGuides.size() + movieGuides.size();
        int failed = 0;

        for (TravelGuide currentTG : travelGuides) {
            RecommendationGroup recommendationGroup = new RecommendationGroup();
            recommendationGroup.setType(RecommendationType.PLACE);
            recommendationGroup.setSearchKey(currentTG.getSearchKey());
            recommendationGroup.setCreationTime(currentTG.getCreationTime());
             
            for (TravelRecommendation tr : currentTG.getTravelRecommendations()) {
                Recommendation recommendation = new Recommendation();
                recommendation.setName(tr.getName());
                recommendation.setDescription(tr.getDescription());
                recommendation.setRecommendationGroup(recommendationGroup);
                recommendationGroup.addRecommendation(recommendation);
            }
            
            try {
                this.recommendationRepository.addRecommendationGroup(recommendationGroup);   
            }
            catch (Exception e) {
                log.error("Migrating {} failed!", recommendationGroup.getSearchKey(), e);
                ++failed;
            }
        }
        
        for (BookGuide currentBG : bookGuides) {
            RecommendationGroup recommendationGroup = new RecommendationGroup();
            recommendationGroup.setType(RecommendationType.BOOK);
            recommendationGroup.setSearchKey(currentBG.getSearchKey());
            recommendationGroup.setCreationTime(currentBG.getCreationTime());
             
            for (BookRecommendation br : currentBG.getBookRecommendations()) {
                Recommendation recommendation = new Recommendation();
                recommendation.setName(br.getName());
                recommendation.setDescription(br.getDescription());
                recommendation.setRecommendationGroup(recommendationGroup);
                recommendationGroup.addRecommendation(recommendation);
            }
            
            try {
                this.recommendationRepository.addRecommendationGroup(recommendationGroup);   
            }
            catch (Exception e) {
                log.error("Migrating {} failed!", recommendationGroup.getSearchKey(), e);
                ++failed;
            }
        }
                
        for (MovieGuide currentMG : movieGuides) {
            RecommendationGroup recommendationGroup = new RecommendationGroup();
            recommendationGroup.setType(RecommendationType.MOVIE);
            recommendationGroup.setSearchKey(currentMG.getSearchKey());
            recommendationGroup.setCreationTime(currentMG.getCreationTime());
             
            for (MovieRecommendation mr : currentMG.getMovieRecommendations()) {
                Recommendation recommendation = new Recommendation();
                recommendation.setName(mr.getName());
                recommendation.setDescription(mr.getDescription());
                recommendation.setRecommendationGroup(recommendationGroup);
                recommendationGroup.addRecommendation(recommendation);
            }
            try {
                this.recommendationRepository.addRecommendationGroup(recommendationGroup);   
            }
            catch (Exception e) {
                log.error("Migrating {} failed!", recommendationGroup.getSearchKey(), e);
                ++failed;
            }
        }
        
        log.info("Migration of {}/{} was successful!", total - failed, total);
        
    }
    
}
