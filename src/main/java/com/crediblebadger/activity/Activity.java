/*
 *  Copyright © 2025 Michail Ostrowski
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
package com.crediblebadger.activity;

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
@NamedQuery(name = Activity.FIND_ACTIVITY_BY_USER_ID, query = "From Activity where userId = :userId order by creationTime DESC")
@NamedQuery(name = Activity.DELETE_ACTIVITY, query = "DELETE FROM Activity activity WHERE activity.id = :id AND activity.userId = :userId")
public class Activity {   
    public static final String FIND_ACTIVITY_BY_USER_ID = "Activity_FindActivityByUserId";
    public static final String DELETE_ACTIVITY = "Activity_DeleteActivity";
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private Long userId;

    @Column
    private LocalDateTime creationTime;
    
    @Column
    private String name;
    
    @Column
    private ActivityCategory category;
    
    @Column
    private int rating;
    
    @Version
    private Long version;
}
