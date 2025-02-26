package com.crediblebadger.user;

import java.time.LocalDateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserServiceTest {
    @Autowired
    private UserService userService;   
    
    @Autowired
    private UserRepository userRepository;   

    @Test
    public void testMarketing() {
        String userEmail = "test_" + System.currentTimeMillis() + "@crediblebadger.com";
        User testUser = new User();
        testUser.setEmail(userEmail);
        testUser.setPassword("testPassword");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setEmailVerified(true);
        testUser.setSubscribedToMarketing(true);
        
        int marketingRecipientsStart = this.userService.retrieveUsersForMarketing().size();
        
        this.userRepository.addUser(testUser);
        
        int marketingRecipientsAfterAdd = this.userService.retrieveUsersForMarketing().size();
        
        String optOutToken = this.userService.generateOptOutToken(userEmail);
        Assertions.assertNotNull(optOutToken);
        boolean optOutResult = this.userService.disableMarketingSubscription(optOutToken);
        Assertions.assertTrue(optOutResult);
        
        testUser = this.userRepository.retrieveUser(userEmail);
        Assertions.assertFalse(testUser.isSubscribedToMarketing());
        
        int marketingRecipientsAfterOptOut = this.userService.retrieveUsersForMarketing().size();
        
        boolean optInResult = this.userService.enableMarketingSubscription(userEmail);
        Assertions.assertTrue(optInResult);
        testUser = this.userRepository.retrieveUser(userEmail);
        Assertions.assertTrue(testUser.isSubscribedToMarketing());
        
        int marketingRecipientsAfterOptIn = this.userService.retrieveUsersForMarketing().size();
        
        Assertions.assertEquals(marketingRecipientsStart, marketingRecipientsAfterAdd - 1);
        Assertions.assertEquals(marketingRecipientsStart, marketingRecipientsAfterOptOut);
        Assertions.assertEquals(marketingRecipientsStart, marketingRecipientsAfterOptIn - 1);
    }
}
