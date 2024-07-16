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

package com.crediblebadger.token;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Data
@NamedQuery(name = SecurityToken.FIND_SECURITY_TOKEN, 
        query = "From SecurityToken where token = :token")
public class SecurityToken {
    public static final String FIND_SECURITY_TOKEN = "SecurityToken_FindSecurityToken";
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id = 0l;
    @Version
    private Long version;
    @Column
    private Long userId;
    @Column
    private String token;
    @Column
    @Enumerated(EnumType.ORDINAL)
    private TokenType type;
    @Column
    private LocalDateTime validUntil;
    @Column
    private boolean burned;
}
