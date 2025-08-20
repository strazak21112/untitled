package pl.wiktor.koprowski.DTO.basic;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class ReadingDTO {

    private Long id;
    private double coldWaterValue;
    private double hotWaterValue;
    private double heatingValue;
    private double electricityValue;
    private LocalDate date;
    private LocalDate billingStartDate;
    private LocalDate billingEndDate;
    private Long apartmentId;
    private Long invoiceId;

    public ReadingDTO(Long id, double coldWaterValue, double hotWaterValue, double heatingValue, double electricityValue,
                      LocalDate date, LocalDate billingStartDate, LocalDate billingEndDate,
                      Long apartmentId, Long invoiceId) {
        this.id = id;
        this.coldWaterValue = coldWaterValue;
        this.hotWaterValue = hotWaterValue;
        this.heatingValue = heatingValue;
        this.electricityValue = electricityValue;
        this.date = date;
        this.billingStartDate = billingStartDate;
        this.billingEndDate = billingEndDate;
        this.apartmentId = apartmentId;
        this.invoiceId = invoiceId;
    }
}
