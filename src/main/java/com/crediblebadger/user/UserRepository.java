package com.crediblebadger.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public class UserRepository {
    
    @PersistenceContext
    private EntityManager entityManager;

    public void addUser(User user) {
        this.entityManager.persist(user);
    }
    
    public User retrieveUser(String email) {
        TypedQuery<User> userQuery = this.entityManager.createNamedQuery(User.FIND_USER_BY_EMAIL, User.class);
        userQuery.setParameter("email", email);
        List<User> results = userQuery.getResultList();
        User result = results.isEmpty() ? null : results.get(0);
        return result;
    }
    
    public User retrieveUser(long userId) {
        TypedQuery<User> userQuery = this.entityManager.createNamedQuery(User.FIND_USER_BY_ID, User.class);
        userQuery.setParameter("id", userId);
        List<User> results = userQuery.getResultList();
        User result = results.isEmpty() ? null : results.get(0);
        return result;
    }
    
    public List<User> retrieveUsers() {
        TypedQuery<User> userQuery = this.entityManager.createNamedQuery(User.LIST_USERS, User.class);
        List<User> results = userQuery.getResultList();
        return results;
    }

    public boolean markEmailAsVerfied(long userId) {
        User user = retrieveUser(userId);
        
        if (user == null) {
            return false;
        }
        
        user.setEmailVerified(true);
        return true;
    }
    
    public boolean changeSuspensionStatus(long userId, boolean isSuspended) {
        User user = retrieveUser(userId);
        
        if (user == null) {
            return false;
        }
        
        user.setSuspended(isSuspended);
        return true;
    }
    
    public boolean changePassword(long userId, String password) {
        User user = retrieveUser(userId);
        
        if (user == null) {
            return false;
        }
        
        user.setPassword(password);
        return true;
    }
}
