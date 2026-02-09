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
package com.crediblebadger.feedback;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@NamedQuery(name = Feedback.FIND_FEEDBACK_BY_PROJECT_KEY, query = "From Feedback where projectKey = :projectKey order by creationTime DESC")
public class Feedback {   
    public static final String FIND_FEEDBACK_BY_PROJECT_KEY = "Feedback_FindFeedbackByProjectKey";
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String projectKey;

    @Column
    private String projectVersion;

    @Column
    private String projectUser;
    
    @Column
    private LocalDateTime creationTime;
    
    @Column
    private String content;
    
    @Version
    private Long version;    
    
    @Column
    private boolean archived;
}
