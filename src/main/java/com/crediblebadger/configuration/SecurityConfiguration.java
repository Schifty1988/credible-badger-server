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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfiguration {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.cors(cors -> cors.configurationSource(buildCorsConfigurationSource()));
        
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers("api/admin/**").hasRole("ADMIN")
                .requestMatchers("api/storage/**").authenticated()
                .requestMatchers("/**", "api/travel/**", "api/user/**").permitAll());
        
        AuthenticationFailureHandler loginFailureHandler = (request, response, authException) -> {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
        };

        AuthenticationSuccessHandler loginSuccessHandler = (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            response.setStatus(HttpStatus.OK.value());
        };
                        
        LogoutSuccessHandler logoutSuccessHandler = (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            response.setStatus(HttpStatus.OK.value());
        };
        
        http.formLogin(login -> login
            .loginProcessingUrl("/api/user/login")
            .loginPage("/login_page")
            .failureHandler(loginFailureHandler)
            .successHandler(loginSuccessHandler));

        http.logout(logout -> logout
            .logoutUrl("/api/user/logout")
            .logoutSuccessHandler(logoutSuccessHandler)
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
            .permitAll());
        return http.build();
    }

    private CorsConfigurationSource buildCorsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("https://crediblebadger.com");
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("http://localhost:8080");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
