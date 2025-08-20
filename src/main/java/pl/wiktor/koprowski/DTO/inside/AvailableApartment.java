package pl.wiktor.koprowski.DTO.inside;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.wiktor.koprowski.domain.Address;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailableApartment{
private Long id;
private int floor;
private String number;
private Address address;
}
