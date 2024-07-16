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

package com.crediblebadger.email;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.stereotype.Service;
import java.io.StringWriter;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.MessageRejectedException;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;

@Service
@Slf4j
public class EmailService {
    private static final int EMAIL_MAX_LENGTH = 320;
    private static final int EMAIL_MAX_LENGTH_LOCAL = 64;
    private static final int EMAIL_MAX_LENGTH_DOMAIN = 255;

    private final SesClient emailClient;

    @Value("${app.baseurl}")
    String baseURL;
    
    @Value("${app.email.from}")    
    private String fromAddress;
    
    private final VelocityEngine velocityEngine;
    
    private Properties emailProperties;
    
    public EmailService(SesClient emailClient) {
        this.emailClient = emailClient;
        
        Properties velocityProperties = new Properties();
        velocityProperties.setProperty("resource.loaders", "class");
        velocityProperties.setProperty("resource.loader.class.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        this.velocityEngine = new VelocityEngine(velocityProperties);
        
        this.emailProperties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("email.properties")) {
            this.emailProperties.load(input);
            log.info("email.properties was loaded!");
        } catch (IOException e) {
            log.error("Loadin email.properties failed!", e);
        }
    }
    
    public boolean sendEmailVerificationRequest(String toAddress, String token) {
        VelocityContext context = new VelocityContext();
        String link = this.baseURL + "verifyEmail/" + token;
        context.put("link", link);
        context.put("email", toAddress);
        String emailBody = renderTemplate("emailVerificationRequest.vm", context);
        String subject = this.emailProperties.getProperty("emailVerificationRequest.subject");
        return sendEmail(toAddress, subject, emailBody);
    }

    public boolean sendPasswordChangeRequestEmail(String toAddress, String token, int lifetime_in_minutes) {
        VelocityContext context = new VelocityContext();
        String link = this.baseURL + "changePassword/" + token;
        context.put("link", link);
        context.put("email", toAddress);
        context.put("lifetime_in_minutes", lifetime_in_minutes);
        String emailBody = renderTemplate("passwordChangeRequest.vm", context);
        String subject = this.emailProperties.getProperty("passwordChangeRequest.subject");
        return sendEmail(toAddress, subject , emailBody);
    }
    
    private String renderTemplate(String templateName, VelocityContext context) {
        StringWriter writer = new StringWriter();
        this.velocityEngine.mergeTemplate("templates/" + templateName, "UTF-8", context, writer);
        return writer.toString();
    }
    
    private boolean sendEmail(String toAddress, String subject, String body) {
        Destination destination = Destination.builder()
                .toAddresses(toAddress)
                .build();

        Content subjectContent = Content.builder().data(subject).build();
        Content bodyContent = Content.builder().data(body).build();
        Body emailBody = Body.builder().text(bodyContent).build();
        
        Message message = Message.builder()
                .subject(subjectContent)
                .body(emailBody)
                .build();
        
        SendEmailRequest request = SendEmailRequest.builder()
                .destination(destination)
                .message(message)
                .source(this.fromAddress)
                .build();
        
        try {
            SendEmailResponse result = this.emailClient.sendEmail(request);
            log.info("Email was sent with result: {}", result);
        }
        catch (MessageRejectedException e) {
            log.info("Email was not sent!", e);            
            return false;
        }
        return true;
    }
    
    /**
     * Simple check to discard a majority of invalid email addresses
     * @param email address to check
     * @return false if the email address is obviously invalid, true otherwise
     */
    public static boolean validateEmail(String email) {
        if (email == null || email.length() > EMAIL_MAX_LENGTH || email.contains("..")) {
            return false;
        }
        
        String[] split = email.split("@");
        
        if (split.length != 2) {
            return false;
        }
        
        String localPart =  split[0];
        String domainPart =  split[1];

        if (localPart.length() == 0 || localPart.length() > EMAIL_MAX_LENGTH_LOCAL || localPart.isBlank()) {
            return false;
        }

        if (domainPart.length() > EMAIL_MAX_LENGTH_DOMAIN) {
            return false;
        }
        
        return domainPart.matches(".+\\.[a-zA-Z]{2,6}");
    }
}
