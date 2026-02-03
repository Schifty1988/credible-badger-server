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

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Version;
import lombok.Data;

@Data
@Entity
public class Recommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id = 0l;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="recommendationGroupId")
    @JsonIgnore
    private RecommendationGroup recommendationGroup;
    
    @Column
    private String name;
    
    @Column
    private String description;
    
    @Column
    private int likes;
        
    @Version
    private Long version;
}
