package pl.wiktor.koprowski.DTO;

import lombok.Data;

@Data
public class UserCredentials {
    private String username;
    private String password;
    private String role;
}