package com.crediblebadger.user;

import lombok.Data;

@Data
public class UserRequestDTO {
    private String email;
    private String password;
    private String securityToken;
    
    public String normalizeEmail() {
        return this.email == null ? null : this.email.trim().toLowerCase();
    }
}
