package com.crediblebadger.user;

import com.crediblebadger.user.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;
    
    @GetMapping("/me")    
    public User me(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userDetails.getUser();
    }

    @PostMapping("/register")
    ResponseEntity registerUser(@RequestBody UserRequestDTO user) {
        boolean result = this.userService.register(user.getLowerCaseEmail(), user.getPassword());
        
        if (result) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/login_page")    
    ResponseEntity<String> loginPage(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam(name="status", defaultValue = "") String status) {
        
        String message = "User=" + userDetails + " status=" + status; 
        return ResponseEntity.ok().body(message);    
    }

    @PostMapping("/requestPasswordChange")
    ResponseEntity requestPasswordChange(@RequestBody UserRequestDTO user) {       
        boolean result = this.userService.requestPasswordChange(user.getLowerCaseEmail());
        
        if (result) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
    
    @PostMapping("/changePassword")
    ResponseEntity changePassword(@RequestBody UserRequestDTO user) {
        boolean result = this.userService.changePassword(user.getSecurityToken(), user.getPassword());
        
        if (result) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/requestEmailVerification")   
    ResponseEntity requestEmailVerification(@RequestBody UserRequestDTO user) {
        boolean result = this.userService.requestEmailVerification(user.getLowerCaseEmail());
        
        if (result) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
    
    @PostMapping("/verifyEmail")   
    ResponseEntity verifyEmail(@RequestBody UserRequestDTO user) {
        boolean result = this.userService.verifyEmail(user.getSecurityToken());
        
        if (result) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }
}