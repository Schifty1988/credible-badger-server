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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@Transactional
public class MarketingRepository {
    
    @PersistenceContext
    EntityManager entityManager;
    
    public boolean storeMarketingCampaign(MarketingCampaign marketingCampaign) {
        Long marketingId = marketingCampaign.getId();
        
        if (marketingCampaign.getId() == null) {
            this.entityManager.persist(marketingCampaign);
            return true;
        }
        
        MarketingCampaign storedCampaign = 
                retrieveMarketingCampaign(marketingId);
            
        if (storedCampaign.getSentAt() != null) {
            log.error("Can't update campaign={}: campaign already started!", marketingId);
            return false;
        }
        
        marketingCampaign.setSentAt(LocalDateTime.now());
        marketingCampaign.setNumberOfViews(0);

        this.entityManager.merge(marketingCampaign);
        return true;
    }
    
    public boolean increaseViewCounter(long id) {
        MarketingCampaign campaign = retrieveMarketingCampaign(id);
        
        if (campaign == null) {
            log.error("Campaign={} not found!", id);
            return false;
        }
        
        if (campaign.getSentAt() == null) {
            log.error("Campaign={} has not been started yet!", id);
            return false;
        }
        
        int currentViews = campaign.getNumberOfViews();
        campaign.setNumberOfViews(currentViews + 1);
        return true;
    }    
    
    public MarketingCampaign retrieveMarketingCampaign(long id) {
        return this.entityManager.find(MarketingCampaign.class, id);
    }

    public List<MarketingCampaign> retrieveAllMarketingCampaigns() {
        TypedQuery<MarketingCampaign> userQuery = this.entityManager.createNamedQuery(MarketingCampaign.LIST_ALL_MARKETING_CAMPAIGNS, MarketingCampaign.class);
        List<MarketingCampaign> results = userQuery.getResultList();
        return results;
    }
    
    public boolean deleteMarketingCampaign(long marketingId) {
        MarketingCampaign campaign = this.retrieveMarketingCampaign(marketingId);
        
        if (campaign == null) {
            return false;
        }
        
        this.entityManager.remove(campaign);
        return true;
    }
    
}
