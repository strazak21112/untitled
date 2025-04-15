package pl.wiktor.koprowski.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class ApartmentDTO {

    @NotBlank(message = "Apartment number cannot be blank")
    private String number;

    @Min(value = 1, message = "Area must be greater than 0")
    private double area;

    @Min(value = 0, message = "Floor must be 0 or higher")
    private int floor;

    private Long buildingId;

    private Long tenantId;

    private List<Long> readingIds;

 }
