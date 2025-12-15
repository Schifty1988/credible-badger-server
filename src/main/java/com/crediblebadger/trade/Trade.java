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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
@Entity
@NamedQuery(name = Trade.FIND_TRADE_BY_USER_ID, query = "From Trade where userId = :userId order by sellDate DESC")
@NamedQuery(name = Trade.DELETE_TRADE, query = "DELETE FROM Trade trade WHERE trade.id = :id AND trade.userId = :userId")
public class Trade {   
    public static final String FIND_TRADE_BY_USER_ID = "Trade_FindTradeByUserId";
    public static final String DELETE_TRADE = "Trade_DeleteTrade";
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private Long userId;
    
    @Column
    private String symbol;

    @Column
    private LocalDate purchaseDate;
    
    @Column
    private BigDecimal purchasePrice;
    
    @Column
    private LocalDate sellDate;
    
    @Column
    private BigDecimal sellPrice;
    
    @Column
    private Integer quantity;

    @Version
    private Long version;
}
