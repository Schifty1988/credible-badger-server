package com.crediblebadger.configuration;

import com.crediblebadger.user.User;
import com.crediblebadger.user.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    UserService userService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = null;
        
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (UserService.ACCESS_TOKEN_COOKIE.equals(cookie.getName())) {
                    token = cookie.getValue();
                }
            }   
        }
        
        if (token == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No Access Token");
            log.error("No JWT Token was sent for request {}!", request.getRequestURI());
            return;
        }
        
        try {
            Claims claims = this.userService.verifyAccessToken(token);
            User user = this.userService.createUserFromClaims(claims);
            
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            user, 
                            null,
                            user.getAuthorities()
                    );
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (JwtException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT");
            log.error("Invalid JWT was sent for url {}!", request.getRequestURI(), e);
            return;
        }
        filterChain.doFilter(request, response);
    }
}

