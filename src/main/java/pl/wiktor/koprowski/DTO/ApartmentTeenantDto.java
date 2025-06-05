package pl.wiktor.koprowski.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApartmentTeenantDto {
    private Long id;
    private String firstName;
    private String lastName;
}
