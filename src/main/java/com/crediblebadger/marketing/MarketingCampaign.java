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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.Data;

@Entity
@Data
@NamedQuery(name = MarketingCampaign.LIST_ALL_MARKETING_CAMPAIGNS, query = "From MarketingCampaign order by id desc")
public class MarketingCampaign {
    public static final String LIST_ALL_MARKETING_CAMPAIGNS = "MarketingCampaign_ListAllMarketingCampaigns";
        
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column
    private String subject;    
    @Column
    private String content;   
    @Column
    private LocalDateTime sentAt;
    @Column
    private int numberOfRecipients;    
    @Column
    private int numberOfDeliveries;  
    @Column
    private int numberOfViews;
    @Version
    private Long version;
}
