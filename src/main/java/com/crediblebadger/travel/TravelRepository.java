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
package com.crediblebadger.travel;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Repository;


@Repository
@Transactional
public class TravelRepository { 
    @PersistenceContext
    private EntityManager entityManager;
    
    public TravelGuide retrieveGuide(String searchKey) {
        TypedQuery<TravelGuide> travelGuideQuery = this.entityManager.createNamedQuery(TravelGuide.FIND_TRAVEL_GUIDE_BY_KEY, TravelGuide.class);
        travelGuideQuery.setParameter("searchKey", searchKey);
        List<TravelGuide> results = travelGuideQuery.getResultList();
        TravelGuide result = results.isEmpty() ? null : results.get(0);
        return result;
    }
    
    public void addTravelGuide(TravelGuide travelGuide) {
        this.entityManager.persist(travelGuide);
    }
    
    public boolean removeTravelGuide(long travelGuideId) {
        TravelGuide travelGuide = this.entityManager.find(TravelGuide.class, travelGuideId);
        
        if (travelGuide == null) {
            return false;
        }
        
        this.entityManager.remove(travelGuide);
        return true;
    }

    public List<TravelGuide> getAllGuides() {
        TypedQuery<TravelGuide> travelGuideQuery = this.entityManager.createQuery("From TravelGuide", TravelGuide.class);
        return travelGuideQuery.getResultList();
    }
}
