package pl.wiktor.koprowski.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivationTokenCreateDTO {
    private String token;
    private LocalDateTime expiresAt;
    private Long userId;
}
