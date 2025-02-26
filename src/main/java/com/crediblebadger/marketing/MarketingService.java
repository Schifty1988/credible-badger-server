/*
 *  Copyright © 2025 Michail Ostrowski
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
package com.crediblebadger.marketing;

import com.crediblebadger.email.EmailService;
import com.crediblebadger.user.User;
import com.crediblebadger.user.UserService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MarketingService {
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private MarketingRepository marketingRepository;
    
    public List<MarketingCampaign> retrieveAllMarketingCampaigns() {
        return this.marketingRepository.retrieveAllMarketingCampaigns();
    }
    
    public boolean startMarketingCampaign(long id) {      
        MarketingCampaign marketingCampaign = 
                this.marketingRepository.retrieveMarketingCampaign(id);
        
        if (marketingCampaign == null || marketingCampaign.getSentAt() != null) {
            return false;
        }
        
        String subject = marketingCampaign.getSubject();
        String content = marketingCampaign.getContent();
        
        List<User> recipients = this.userService.retrieveUsersForMarketing();
        int numberOfRecipients = recipients.size();
        log.info("Sending marketing email={} to {} users!", subject, recipients.size());
        
        int numberOfDeliveries = 0;
        
        for (User currentUser : recipients) {
            String currentEmail = currentUser.getEmail();
            String currentToken = this.userService.generateOptOutToken(currentEmail);
            boolean currentResult =
                    this.emailService.sendMarketingEmail(currentEmail, subject, content, currentToken, id);
            
            if (currentResult == false) {
                log.error("Marketing email={} could not be sent to user={}", subject, currentEmail);
                continue;
            }
            
            ++numberOfDeliveries;
        }
        
        marketingCampaign.setNumberOfRecipients(numberOfRecipients);
        marketingCampaign.setNumberOfDeliveries(numberOfDeliveries);
        
        this.marketingRepository.storeMarketingCampaign(marketingCampaign);
        return true;
    }
    
    public void storeMarketingCampaign(MarketingCampaign marketingCampaign) { 
        this.marketingRepository.storeMarketingCampaign(marketingCampaign);
    }

    public boolean increaseCampaignViews(long id) {
        return this.marketingRepository.increaseViewCounter(id);
    }
}
