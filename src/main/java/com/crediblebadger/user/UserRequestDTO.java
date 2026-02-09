package com.crediblebadger.user;

import java.util.UUID;
import lombok.Data;

@Data
public class UserRequestDTO {
    private String email;
    private String password;
    private UUID securityToken;
    
    public String normalizeEmail() {
        return this.email == null ? null : this.email.trim().toLowerCase();
    }
}
