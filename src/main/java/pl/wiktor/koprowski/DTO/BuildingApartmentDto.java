package pl.wiktor.koprowski.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuildingApartmentDto {
    private Long id;
    private int floor;
    private String number;
}