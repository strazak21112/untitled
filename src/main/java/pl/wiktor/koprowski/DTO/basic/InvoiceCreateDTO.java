package pl.wiktor.koprowski.DTO.basic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceCreateDTO {

    private Long apartmentId;

    private String issueDate;

}
