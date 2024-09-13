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
import java.util.List;
import lombok.Data;

@Entity
@Data
@NamedQuery(name = User.FIND_USER_BY_EMAIL, query = "From User where email = :email")
@NamedQuery(name = User.FIND_USER_BY_ID, query = "From User where id = :id")
@NamedQuery(name = User.LIST_USERS, query = "From User order by createdAt desc")
public class User implements Serializable {   
    public static final String FIND_USER_BY_EMAIL = "User_findUserByEmail";
    public static final String FIND_USER_BY_ID = "User_findUserById";
    public static final String LIST_USERS = "User_listUsers"; 
    
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
    LocalDateTime createdAt;
    @Column
    boolean emailVerified;  
    @Column
    boolean suspended;  
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name="userId")
    List<Role> roles;
}
