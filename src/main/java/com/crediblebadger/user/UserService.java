package com.crediblebadger.user;

import com.crediblebadger.token.SecurityTokenService;
import com.crediblebadger.email.EmailService;
import com.crediblebadger.token.SecurityToken;
import com.crediblebadger.token.TokenType;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService implements UserDetailsService{ 
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
                .sendPasswordChangeRequestEmail(email, token, SecurityTokenService.SECURITY_TOKEN_LIFETIME_MINUTES);
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
}
