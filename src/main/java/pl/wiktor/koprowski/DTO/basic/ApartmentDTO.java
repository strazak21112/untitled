package pl.wiktor.koprowski.DTO.basic;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import pl.wiktor.koprowski.DTO.inside.ApartmentTenantDto;
import pl.wiktor.koprowski.DTO.inside.BuildingInfoDTO;

@Data
public class ApartmentDTO {

    private Long id;

    @NotBlank(message = "Apartment number cannot be blank")
    private String number;

    @Min(value = 1, message = "Area must be greater than 0")
    private double area;

    @Min(value = 0, message = "Floor must be 0 or higher")
    private int floor;

    private BuildingInfoDTO buildingInfo;

    private ApartmentTenantDto tenant;
 }
