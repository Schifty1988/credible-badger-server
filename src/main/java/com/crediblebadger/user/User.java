package com.crediblebadger.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Version;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Data
@NamedQuery(name = User.FIND_USER_BY_EMAIL, query = "From User where email = :email")
@NamedQuery(name = User.FIND_USER_BY_ID, query = "From User where id = :id")
@NamedQuery(name = User.LIST_ALL_USERS, query = "From User order by createdAt desc")
@NamedQuery(name = User.LIST_USERS_FOR_MARKETING, query = "From User where emailVerified = true AND subscribedToMarketing = true")
public class User implements Serializable, UserDetails {   
    public static final String FIND_USER_BY_EMAIL = "User_findUserByEmail";
    public static final String FIND_USER_BY_ID = "User_findUserById";
    public static final String LIST_ALL_USERS = "User_listAllUsers"; 
    public static final String LIST_USERS_FOR_MARKETING = "User_listUsersForMarketing"; 
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id = 0l;
    @Version
    private Long version;
    @Column
    private String email;
    @Column
    @JsonIgnore
    private String password;
    @Column
    private LocalDateTime createdAt;
    @Column
    private boolean emailVerified;  
    @Column
    private boolean suspended;  
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name="userId")
    private List<Role> roles;  
    @Column    
    private boolean subscribedToMarketing;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream().map(
                role -> new SimpleGrantedAuthority(role.getRole())).collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return this.email;
    }
         
    public static boolean validateUser(User user) {
        return user != null && 
                user.isEnabled()&& 
                user.isEmailVerified();     
    }
}
