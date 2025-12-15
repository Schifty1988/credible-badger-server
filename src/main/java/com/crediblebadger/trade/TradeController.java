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
package com.crediblebadger.trade;

import com.crediblebadger.user.User;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trade")
public class TradeController {
        
    @Autowired
    TradeService tradeService;

    @PostMapping("/submit")    
    public ResponseEntity submitTrade(
            @AuthenticationPrincipal User user,
            @RequestBody Trade trade) {
        trade.setUserId(user.getId());
        this.tradeService.addTrade(trade);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/delete")    
    public ResponseEntity deleteTrade(
            @AuthenticationPrincipal User user,
            @RequestBody Trade trade) {
        trade.setUserId(user.getId());
        boolean result = this.tradeService.deleteTrade(trade);
        
        if (result == false) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/retrieve")
    public ResponseEntity<List<Trade>> retrieveTradeSummary(
            @RequestBody TradeRequest request) {
        return ResponseEntity.ok(this.tradeService.retrieveTradeSummary(request.getUserId()));
    }
}
