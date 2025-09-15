package pl.wiktor.koprowski.DTO.inside;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.wiktor.koprowski.DTO.row.ApartmentRowDTO;

import java.time.LocalDate;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReadingDetails {
    private Long id;
    private double coldWaterValue;
    private double hotWaterValue;
    private double heatingValue;
    private double electricityValue;
    private String date;
    private String billingStartDate;
    private String billingEndDate;
    private ApartmentRowDTO apartment;
}
