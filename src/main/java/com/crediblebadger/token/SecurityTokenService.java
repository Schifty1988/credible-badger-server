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
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SecurityTokenService {
    private static final int TOKEN_LENGTH = 20;
    public static final int SECURITY_TOKEN_LIFETIME_MINUTES = 15;
    
    @Autowired
    private SecurityTokenRepository securityTokenRepository;
    
    public String addToken(long userId, TokenType type) {
        String generatedToken = RandomStringUtils.randomAlphanumeric(TOKEN_LENGTH);
        LocalDateTime validUntil = LocalDateTime.now().plusMinutes(SECURITY_TOKEN_LIFETIME_MINUTES);
        
        SecurityToken token = new SecurityToken();       
        token.setToken(generatedToken);
        token.setValidUntil(validUntil);
        token.setUserId(userId);
        token.setType(type);

        this.securityTokenRepository.addSecurityToken(token);
        return generatedToken;
    }
        
    public boolean burnToken(String securityToken, TokenType type) {
        return this.securityTokenRepository.burnToken(securityToken, type);
    }
    
    public SecurityToken findToken(String securityToken) {
        return this.securityTokenRepository.findToken(securityToken);
    }
}
