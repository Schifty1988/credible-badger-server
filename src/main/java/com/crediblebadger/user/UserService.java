package com.crediblebadger.user;

import com.crediblebadger.token.SecurityTokenService;
import com.crediblebadger.email.EmailService;
import com.crediblebadger.token.SecurityToken;
import com.crediblebadger.token.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService implements UserDetailsService{       
    public static final String ACCESS_TOKEN_COOKIE = "access_token";
    public static final String ACCESS_TOKEN_EXPIRES_AT_COOKIE = "access_token_expires_at";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";   
    public static final long ACCESS_TOKEN_LIFETIME_SECONDS = 60L * 10L;
    public static final long REFRESH_TOKEN_LIFETIME_SECONDS = TokenType.REFRESH_SESSION.getLifetimeInMinutes() * 60;
        
    @Value("${app.jwtSecret}")
    private String jwtSecret;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SecurityTokenService securityTokenService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private static final int PASSWORD_MIN_LENGTH = 6;
    
    public boolean register(String email, String password) {
        if (!EmailService.validateEmail(email) || !validatePassword(password)) {
            log.info("User was not registered: email={} or password failed validation!", email);
            return false;
        }    
        
        if (this.userRepository.retrieveUser(email) != null) {
            log.info("User was not registered: email={} already in use!", email);
            return false;
        }
        
        User user = new User();
        user.setEmail(email);
        String encodedPassword = this.passwordEncoder.encode(password);
        user.setPassword(encodedPassword);
        user.setCreatedAt(LocalDateTime.now());
        user.setSubscribedToMarketing(true);
        this.userRepository.addUser(user);
        log.info("user={} was registered!", email);
        String token = this.securityTokenService.addToken(user.getId(), TokenType.EMAIL_VERIFICATION);
        return this.emailService.sendEmailVerificationRequest(email, token);
    }

    public List<User> listAllUsers() {
        return this.userRepository.retrieveAllUsers();
    }

    public boolean requestEmailVerification(String email) {
        User retrievedUser = this.userRepository.retrieveUser(email);

        if (retrievedUser == null) {
            return false;
        }
        
        if (retrievedUser.isEmailVerified()) {
            return false;
        }
        
        String token = this.securityTokenService.addToken(retrievedUser.getId(), TokenType.EMAIL_VERIFICATION);
        return this.emailService.sendEmailVerificationRequest(email, token);
    }
    
    public boolean requestPasswordChange(String email) {
        User retrievedUser = this.userRepository.retrieveUser(email);
        
        if (retrievedUser == null) {
            return false;
        }
        
        String token = this.securityTokenService
                .addToken(retrievedUser.getId(), TokenType.PASSWORD_CHANGE);
        return this.emailService
                .sendPasswordChangeRequestEmail(email, token, TokenType.PASSWORD_CHANGE.getLifetimeInMinutes());
    }
    
    public boolean changePassword(String token, String newPassword) {
        if (!validatePassword(newPassword)) {
            return false;
        }
        
        SecurityToken securityToken = this.securityTokenService.findToken(token);

        if (securityToken == null) {
            return false;
        }
        
        if (!this.securityTokenService.burnToken(token, TokenType.PASSWORD_CHANGE)) {
            return false;
        }       
        
        String encodedPassword = this.passwordEncoder.encode(newPassword);
        return this.userRepository.changePassword(securityToken.getUserId(), encodedPassword);
    }
    
    public boolean verifyEmail(String token) {
        SecurityToken securityToken = this.securityTokenService.findToken(token);

        if (securityToken == null) {
            return false;
        }
        
        if (!this.securityTokenService.burnToken(token, TokenType.EMAIL_VERIFICATION)) {
            return false;
        }
        return this.userRepository.markEmailAsVerfied(securityToken.getUserId());
    }
    
    private boolean validatePassword(String password) {
        return password != null && password.length() >= PASSWORD_MIN_LENGTH;
    }

    public boolean updateSuspensionStatus(long userId, boolean isSuspended) {
        return this.userRepository.changeSuspensionStatus(userId, isSuspended);
    }
    
    public List<User> retrieveUsersForMarketing() {
        return this.userRepository.retrieveUsersForMarketing();
    }
    
    public boolean enableMarketingSubscription(String userEmail) {
        return this.userRepository.updateMarketingSubscriptionForUser(userEmail, true);
    }
    
    public boolean disableMarketingSubscription(String optOutToken) {
        byte[] decodedBytes = Base64.getDecoder().decode(optOutToken);
        String userEmail = new String(decodedBytes);             
        log.info("User={} is disabling marketing subscription!", userEmail);
        return this.userRepository.updateMarketingSubscriptionForUser(userEmail, false);
    }
      
    public String generateOptOutToken(String userEmail) {
        return Base64.getEncoder().encodeToString(userEmail.getBytes());
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        String lowerCaseUsername = username.toLowerCase();
        User user = this.userRepository.retrieveUser(lowerCaseUsername);  
        if (user == null) {
            throw new UsernameNotFoundException(lowerCaseUsername);
        }
        return user;
    }
    
    public String login(String username, String password) {
        String lowerCaseUsername = username.toLowerCase();
        User user = this.userRepository.retrieveUser(lowerCaseUsername);  
        if (user == null || user.isSuspended()) {
            return null;
        }

        if (!this.passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }
        
        return this.generateAccessToken(user);
    }

    public String createRefreshToken(long userId) {
        String securityToken = this.securityTokenService.addToken(userId, TokenType.REFRESH_SESSION);
        return securityToken;
    }
   
    public String refreshAccessToken(String refreshToken) {
        boolean result = invalidateRefreshToken(refreshToken);
        
        if (!result) {
            return null;
        }
        SecurityToken token = this.securityTokenService.findToken(refreshToken);
        User user = this.userRepository.retrieveUser(token.getUserId());
        
        if(user == null || user.isSuspended()) {
            return null;
        }
        
        return generateAccessToken(user);
    }

    public boolean invalidateRefreshToken(String refreshToken) {
        boolean result = this.securityTokenService.burnToken(refreshToken, TokenType.REFRESH_SESSION);
        return result;
    }

    private SecretKey getTokenKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(User user) {
        SecretKey key = getTokenKey();

        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(ACCESS_TOKEN_LIFETIME_SECONDS);
        
        List<String> roleNames = 
            user.getRoles().stream().map(Role::getRole)
                .collect(Collectors.toList());
        
        JwtBuilder builder = Jwts.builder()
            .subject(user.getUsername())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .claim("roles", roleNames)
            .claim("verified", user.isEmailVerified())
            .claim("userId", user.getId())
            .signWith(key, Jwts.SIG.HS256);
        return builder.compact();
    }
    
    public Claims verifyAccessToken(String token) {
        JwtParser parser = Jwts.parser().verifyWith(getTokenKey()).build();
        Jws<Claims> jws = parser.parseSignedClaims(token);
        return jws.getPayload();
    }
    
    public User createUserFromClaims(Claims claims) {
        User user = new User();
        user.setId(claims.get("userId", Long.class));
        user.setEmail(claims.getSubject());       
        List<String> rolesAsString = claims.get("roles", List.class);
        List<Role> roles = new LinkedList<>();
        for (String currentRoleName : rolesAsString) {
            Role currentRole = new Role();
            currentRole.setRole(currentRoleName);
            roles.add(currentRole);
        }        
        user.setRoles(roles);
        user.setEmailVerified(claims.get("verified", Boolean.class));
        return user;
    }
}
