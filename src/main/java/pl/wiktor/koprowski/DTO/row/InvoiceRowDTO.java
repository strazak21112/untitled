package pl.wiktor.koprowski.DTO.row;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceRowDTO {
    private Long id;
    private boolean paid;

    private String billingStartDate;
    private String billingEndDate;
    private String apartmentNumber;
}
