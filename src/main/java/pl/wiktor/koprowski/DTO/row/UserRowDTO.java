package pl.wiktor.koprowski.DTO.row;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRowDTO {
    private Long id;
    private String email;
    private String role;
}
