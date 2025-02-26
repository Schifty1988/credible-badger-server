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

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/marketing")
public class MarketingController {
    
    @Autowired
    MarketingService marketingService;
    
    @Autowired
    private ResourceLoader resourceLoader;

    @PostMapping("/storeCampaign")
    public ResponseEntity storeCampaign(@RequestBody MarketingCampaign marketingCampaign) {
        this.marketingService.storeMarketingCampaign(marketingCampaign);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/startCampaign/{id}")
    public ResponseEntity startMarketingCampaign(@PathVariable long id) {
        boolean result = this.marketingService.startMarketingCampaign(id);
        
        if (!result) {
            return  ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/listCampaigns")
    public List<MarketingCampaign> retrieveAllMarketingCampaigns() {
        return this.marketingService.retrieveAllMarketingCampaigns();
    }
    
    @GetMapping("/viewCampaign/{id}")
    @CrossOrigin(origins = "*")
    public ResponseEntity<byte[]> viewCampaign(@PathVariable long id) throws IOException {
        this.marketingService.increaseCampaignViews(id);
        
        Resource resource = resourceLoader.getResource("classpath:/static/logo_title.png");
        byte[] imageBytes = resource.getInputStream().readAllBytes();
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(imageBytes);
    }
}
