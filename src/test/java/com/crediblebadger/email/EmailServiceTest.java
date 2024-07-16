package com.crediblebadger.email;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.MessageRejectedException;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

@SpringBootTest
public class EmailServiceTest {
    private SesClient sesClient;
    private EmailService emailService;    

    @BeforeEach
    public void setUp() {
        sesClient = Mockito.mock(SesClient.class);
        emailService = new EmailService(sesClient);
    }       
    
    @Test
    public void sendWelcomeEmail() {
        Assertions.assertTrue(this.emailService.sendEmailVerificationRequest("test@crediblebadger.com", "someToken"));
        Mockito.when(this.sesClient.sendEmail(Mockito.any(SendEmailRequest.class))).thenThrow(MessageRejectedException.class);
        Assertions.assertFalse(this.emailService.sendEmailVerificationRequest("test@crediblebadger.com", "someToken"));
    }
    
    @Test
    public void validateEmailAddress() {
        Assertions.assertTrue(EmailService.validateEmail("test@crediblebadger.com"));
        Assertions.assertFalse(EmailService.validateEmail("test-crediblebadger.com"));
        Assertions.assertFalse(EmailService.validateEmail("test..user@crediblebadger.com"));
        Assertions.assertFalse(EmailService.validateEmail(" @crediblebadger.com"));
        Assertions.assertFalse(EmailService.validateEmail("@crediblebadger.com"));
        Assertions.assertFalse(EmailService.validateEmail("test@"));
        Assertions.assertFalse(EmailService.validateEmail("test@  "));
        Assertions.assertFalse(EmailService.validateEmail("test@de"));
        Assertions.assertFalse(EmailService.validateEmail("test@co..uk"));       
        Assertions.assertFalse(EmailService.validateEmail("test@crediblebadger.c"));
        Assertions.assertFalse(EmailService.validateEmail("test@crediblebadger"));
    }
}
