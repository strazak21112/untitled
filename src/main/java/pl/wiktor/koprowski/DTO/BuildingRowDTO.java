package pl.wiktor.koprowski.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.wiktor.koprowski.domain.Address;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuildingRowDTO {
 private Long id;
 private Address address;
 private int numberOfFloors;
}
