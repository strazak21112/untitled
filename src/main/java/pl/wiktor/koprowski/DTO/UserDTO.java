package pl.wiktor.koprowski.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String telephone;
    private String email;
    private String role;
    private boolean enabled;

    private String pesel;

    private ApartmentDTO apartment;

    private List<BuildingInfoDTO> managedBuilding;
}
