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
package com.crediblebadger.recommendation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@NamedQuery(name = RecommendationLike.FIND_RECOMMENENDATION_LIKE_FOR_USER, query = "From RecommendationLike where userId = :userId and recommendationId = :recommendationId")
@NamedQuery(name = RecommendationLike.LIST_FOR_USER, query = "SELECT rl.recommendationId FROM RecommendationLike rl WHERE rl.userId = :userId")
@NamedQuery(name = RecommendationLike.DELETE_ALL_LIKES, query = "DELETE RecommendationLike where recommendationId = :recommendationId")
public class RecommendationLike {
    public static final String LIST_FOR_USER = "RECOMMENDATION_LIKE_LIST_FOR_USER";
    public static final String DELETE_ALL_LIKES = "RECOMMENDATION_DELETE_ALL_LIKES_FOR_RECOMMENDATION";
    public static final String FIND_RECOMMENENDATION_LIKE_FOR_USER = "RECOMMENDATION_LIKE_FIND_LIKE_FOR_USER";
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id = 0l;
    
    @Column
    private Long userId;

    @Column
    private UUID recommendationId;
}
