package pl.wiktor.koprowski.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.*;
import pl.wiktor.koprowski.domain.Address;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuildingDTO {

    private Long id;

    private Address address;

    private List<Long> apartments;

    private List<Long> managers;

    @DecimalMin(value = "0.01", message = "Electricity rate must be greater than 0")
    private double electricityRate;

    @DecimalMin(value = "0.01", message = "Cold water rate must be greater than 0")
    private double coldWaterRate;

    @DecimalMin(value = "0.01", message = "Hot water rate must be greater than 0")
    private double hotWaterRate;

    @DecimalMin(value = "0.01", message = "Heating rate must be greater than 0")
    private double heatingRate;

    @DecimalMin(value = "0.01", message = "Rent rate per m² must be greater than 0")
    private double rentRatePerM2;

    @DecimalMin(value = "0.01", message = "Other charges per m² must be greater than 0")
    private double otherChargesPerM2;

    @DecimalMin(value = "0.01", message = "Flat electricity rate must be greater than 0")
    private double flatElectricityRate;

    @DecimalMin(value = "0.01", message = "Flat cold water rate must be greater than 0")
    private double flatColdWaterRate;

    @DecimalMin(value = "0.01", message = "Flat hot water rate must be greater than 0")
    private double flatHotWaterRate;

    @DecimalMin(value = "0.01", message = "Flat heating rate must be greater than 0")
    private double flatHeatingRate;

    @Min(value = 1, message = "Number of floors must be at least 1")
    private int numberOfFloors;

    public void updateRates(double electricityRate, double coldWaterRate, double hotWaterRate, double heatingRate,
                            double rentRatePerM2, double otherChargesPerM2,
                            double flatElectricityRate, double flatColdWaterRate, double flatHotWaterRate, double flatHeatingRate) {
        this.electricityRate = electricityRate;
        this.coldWaterRate = coldWaterRate;
        this.hotWaterRate = hotWaterRate;
        this.heatingRate = heatingRate;
        this.rentRatePerM2 = rentRatePerM2;
        this.otherChargesPerM2 = otherChargesPerM2;
        this.flatElectricityRate = flatElectricityRate;
        this.flatColdWaterRate = flatColdWaterRate;
        this.flatHotWaterRate = flatHotWaterRate;
        this.flatHeatingRate = flatHeatingRate;
    }
}
