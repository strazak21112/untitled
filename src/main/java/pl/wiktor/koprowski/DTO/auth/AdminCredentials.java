package pl.wiktor.koprowski.DTO.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCredentials {

    private String username;
    private String password;
}
