package pl.wiktor.koprowski.DTO.basic;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import pl.wiktor.koprowski.domain.InvoiceInfo;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDTO {
    private Long id;

    @Positive(message = "Rent amount must be greater than 0")
    @DecimalMin(value = "0.01", message = "Rent amount must be greater than 0")
    private double rentAmount;

    @Positive(message = "Other charges must be greater than 0")
    @DecimalMin(value = "0.01", message = "Other charges must be greater than 0")
    private double otherCharges;

    @Positive(message = "Total media amount must be greater than 0")
    @DecimalMin(value = "0.01", message = "Total media amount must be greater than 0")
    private double totalMediaAmount;

    @Positive(message = "Total amount must be greater than 0")
    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    private double totalAmount;


    @NotNull(message = "Issue date cannot be null")

    private String issueDate;

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    private Long readingId;

    private boolean paid;

    @NotNull(message = "Apartment ID cannot be null")
    private Long apartmentId;

    @NotNull(message = "Billing start date cannot be null")

    private String billingStartDate;

    @NotNull(message = "Billing end date cannot be null")

    private String billingEndDate;

    private boolean confirmed;

    boolean flat;

    InvoiceInfo info;


}
