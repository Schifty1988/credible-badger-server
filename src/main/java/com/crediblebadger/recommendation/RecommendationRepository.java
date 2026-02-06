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
package com.crediblebadger.recommendation;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;


@Repository
@Transactional
public class RecommendationRepository { 
    @PersistenceContext
    private EntityManager entityManager;
    
    public RecommendationGroup retrieveRecommendationGroup(String searchKey, RecommendationType type) {
        TypedQuery<RecommendationGroup> recommendationGroupQuery = 
                this.entityManager.createNamedQuery(RecommendationGroup.FIND_RECOMMENDATION_GROUP_BY_KEY, RecommendationGroup.class);
        recommendationGroupQuery.setParameter("searchKey", searchKey);
        recommendationGroupQuery.setParameter("type", type);
        List<RecommendationGroup> results = recommendationGroupQuery.getResultList();
        RecommendationGroup result = results.isEmpty() ? null : results.get(0);
        return result;
    }
    
    public void addRecommendationGroup(RecommendationGroup recommendationGroup) {
        this.entityManager.persist(recommendationGroup);
    }
    
    public boolean removeRecommendationGroup(long recommendationGroupId) {
        RecommendationGroup recommendationGroup = this.entityManager.find(RecommendationGroup.class, recommendationGroupId);
        
        if (recommendationGroup == null) {
            return false;
        }
        
        this.entityManager.remove(recommendationGroup);
        return true;
    }
    
    public void likeRecommendation(Long userId, UUID recommendationId) {
        RecommendationLike recommendationLike = new RecommendationLike();
        recommendationLike.setUserId(userId);
        recommendationLike.setRecommendationId(recommendationId);
        this.entityManager.persist(recommendationLike);
        
        Recommendation recommendation = 
            this.entityManager.find(Recommendation.class, recommendationId);
        recommendation.setLikes(recommendation.getLikes() + 1);
    }
    
    public void unlikeRecommendation(Long userId, UUID recommendationId) {
        TypedQuery<RecommendationLike> recommendationGroupQuery = 
                this.entityManager.createNamedQuery(RecommendationLike.FIND_RECOMMENENDATION_LIKE_FOR_USER, RecommendationLike.class);
        recommendationGroupQuery.setParameter("userId", userId);
        recommendationGroupQuery.setParameter("recommendationId", recommendationId);
        
        List<RecommendationLike> results = recommendationGroupQuery.getResultList();
        RecommendationLike result = results.isEmpty() ? null : results.get(0);

        this.entityManager.remove(result);
        
        Recommendation recommendation = 
            this.entityManager.find(Recommendation.class, recommendationId);
        recommendation.setLikes(recommendation.getLikes() - 1);
    }

    public List<UUID> retrieveLikedRecommendations(Long userId) {
        TypedQuery<UUID> recommendationGroupQuery = 
        this.entityManager.createNamedQuery(RecommendationLike.LIST_FOR_USER, UUID.class);
        recommendationGroupQuery.setParameter("userId", userId);
        
        List<UUID> results = recommendationGroupQuery.getResultList();
        return results;
    }
}
