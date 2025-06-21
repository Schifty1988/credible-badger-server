package com.crediblebadger.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    UserService userService;
    
    @GetMapping("/me")    
    public ResponseEntity<User> me(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(user);
    }

    @PostMapping("/register")
    public ResponseEntity registerUser(@RequestBody UserRequestDTO user) {
        boolean result = this.userService.register(user.normalizeEmail(), user.getPassword());
        
        if (result) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/requestPasswordChange")
    public ResponseEntity requestPasswordChange(@RequestBody UserRequestDTO user) {       
        boolean result = this.userService.requestPasswordChange(user.normalizeEmail());
        
        if (result) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
    
    @PostMapping("/changePassword")
    public ResponseEntity changePassword(@RequestBody UserRequestDTO user) {
        boolean result = this.userService.changePassword(user.getSecurityToken(), user.getPassword());
        
        if (result) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/requestEmailVerification")   
    public ResponseEntity requestEmailVerification(@RequestBody UserRequestDTO user) {
        boolean result = this.userService.requestEmailVerification(user.normalizeEmail());
        
        if (result) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
    
    @PostMapping("/verifyEmail")   
    public ResponseEntity verifyEmail(@RequestBody UserRequestDTO user) {
        boolean result = this.userService.verifyEmail(user.getSecurityToken());
        
        if (result) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
    
    @PostMapping("/enableMarketingSubscription")   
    public ResponseEntity enableMarketingSubscription(
            @AuthenticationPrincipal User user) {
        
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        
        boolean result = 
            this.userService.enableMarketingSubscription(user.getUsername());
        
        if (!result) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();        
    }
    
    /**
     * We are using generated tokens which are sent out in marketing emails 
     * instead of user credentials to enable users to deactivate the marketing 
     * subscription without logging in first.
     * 
     * @param optOutToken encrypted token that links to the corresponding user
     * @return 200 if subscription was disabled, 400 otherwise
     */
    @PostMapping("/disableMarketingSubscription/{optOutToken}")   
    public ResponseEntity disableMarketingSubscription(@PathVariable String optOutToken) {
       
        if (optOutToken == null || optOutToken.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        
        boolean result = 
            this.userService.disableMarketingSubscription(optOutToken);
        
        if (!result) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();     
    }
}
