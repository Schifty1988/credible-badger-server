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

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SecurityTokenService {
    @Autowired
    private SecurityTokenRepository securityTokenRepository;
    
    public UUID addToken(long userId, TokenType type) {
        LocalDateTime validUntil = LocalDateTime.now().plusMinutes(type.getLifetimeInMinutes());   
        SecurityToken token = new SecurityToken();       
        token.setValidUntil(validUntil);
        token.setUserId(userId);
        token.setType(type);

        this.securityTokenRepository.addSecurityToken(token);
        return token.getId();
    }
        
    public boolean burnToken(UUID securityToken, TokenType type) {
        return this.securityTokenRepository.burnToken(securityToken, type);
    }
    
    public SecurityToken findToken(UUID securityToken) {
        return this.securityTokenRepository.findToken(securityToken);
    }
}
