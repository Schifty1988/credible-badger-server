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
import jakarta.persistence.OrderBy;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import lombok.Data;

@Data
@Entity
@NamedQuery(name = RecommendationGroup.FIND_RECOMMENDATION_GROUP_BY_KEY, query = "From RecommendationGroup where searchKey = :searchKey AND type = :type")
public class RecommendationGroup {
    public static final String FIND_RECOMMENDATION_GROUP_BY_KEY = "RecommendationGroup_FindRecommendationGroupBySearchKey";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id = 0l;
    
    @Column
    private String searchKey;
    
    @Column
    private RecommendationType type;
    
    @Column
    private LocalDateTime creationTime;
            
    @Version
    private Long version;
    
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST, orphanRemoval = true)
    @JoinColumn(name="recommendationGroupId")
    @OrderBy("likes DESC")
    private List<Recommendation> recommendations = new LinkedList<>();
    
    public void addRecommendation(Recommendation recommendation) {
        recommendation.setRecommendationGroup(this);
        this.recommendations.add(recommendation);
    }
}
