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

public enum TokenType {
    EMAIL_VERIFICATION(15),
    PASSWORD_CHANGE(15),
    REFRESH_SESSION(60 * 24 * 15); // 15 days

    private final int lifetimeInMinutes;
    
    private TokenType(int lifetimeInMinutes) {
        this.lifetimeInMinutes = lifetimeInMinutes;
    }

    public int getLifetimeInMinutes() {
        return lifetimeInMinutes;
    }
}
