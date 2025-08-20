package pl.wiktor.koprowski.DTO.row;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.wiktor.koprowski.domain.Address;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class ApartmentRowDTO {

        private Long id;
        private Address address;
        private String number;
    }
