package pl.wiktor.koprowski.DTO.auth;

import lombok.Data;

@Data
public class UserCredentials {
    private String username;
    private String password;
    private String role;
}