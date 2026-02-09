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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public class SecurityTokenRepository {
    @PersistenceContext
    private EntityManager entityManager;
    
    public void addSecurityToken(SecurityToken securityToken) {
        this.entityManager.persist(securityToken);
    }
        
    public boolean burnToken(UUID token, TokenType type) {
        SecurityToken securityToken = findToken(token);
        
        if (securityToken == null) {
            return false;
        }
        
        if (securityToken.getType() != type) {
            return false;
        }
        
        if (LocalDateTime.now().isAfter(securityToken.getValidUntil())) {
            return false;
        }
        
        if (securityToken.isBurned()) {
            return false;
        }
        securityToken.setBurned(true);
        return true;
    }

    SecurityToken findToken(UUID securityToken) {
        return this.entityManager.find(SecurityToken.class, securityToken);
    }
}
