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

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public class TradeRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public boolean addTrade(Trade trade) {
        this.entityManager.persist(trade);
        return true;
    }
    
    public boolean deleteTrade(Trade trade) {
        Query deleteQuery = this.entityManager.createNamedQuery(Trade.DELETE_TRADE);
        deleteQuery.setParameter("id", trade.getId());
        deleteQuery.setParameter("userId", trade.getUserId());
        int result = deleteQuery.executeUpdate();
        return result == 1;
    }

    public List<Trade> retrieveTrades(long userId) {
        TypedQuery<Trade> userQuery = this.entityManager.createNamedQuery(Trade.FIND_TRADE_BY_USER_ID, Trade.class);
        userQuery.setParameter("userId", userId);
        List<Trade> results = userQuery.getResultList();
        return results;
    }
}
