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

package com.crediblebadger.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(authorize -> authorize.disable());
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/user/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/storage/**").authenticated());

        http.formLogin(login -> login
                        .loginPage("/user/login_page?status=not_logged_in")
                        .loginProcessingUrl("/user/login")
                        .defaultSuccessUrl("/user/login_page?status=login_success", true)
                        .failureUrl("/user/login_page?status=login_failure"));
        http.logout(logout -> logout
                .logoutUrl("/user/logout")
                .logoutSuccessUrl("/user/login_page?status=logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll());
        return http.build();
    }
}
