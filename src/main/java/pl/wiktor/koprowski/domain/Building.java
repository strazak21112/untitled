package pl.wiktor.koprowski.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "buildings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Building {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Embedded
    Address address;

    @OneToMany(mappedBy = "building", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Apartment> apartments = new ArrayList<>();

    @ManyToMany(mappedBy = "managedBuildings", fetch = FetchType.EAGER)
    private List<User> managers = new ArrayList<>();


    @Column(nullable = false)
    @DecimalMin(value = "0.01", message = "Electricity rate must be greater than 0")
    double electricityRate;

    @Column(nullable = false)
    @DecimalMin(value = "0.01", message = "Cold water rate must be greater than 0")
    double coldWaterRate;

    @Column(nullable = false)
    @DecimalMin(value = "0.01", message = "Hot water rate must be greater than 0")
    double hotWaterRate;

    @Column(nullable = false)
    @DecimalMin(value = "0.01", message = "Heating rate must be greater than 0")
    double heatingRate;

    @Column(nullable = false)
    @DecimalMin(value = "0.01", message = "Rent rate per m² must be greater than 0")
    double rentRatePerM2;

    @Column(nullable = false)
    @DecimalMin(value = "0.01", message = "Other charges per m² must be greater than 0")
    double otherChargesPerM2;

    @Column(nullable = false)
    @DecimalMin(value = "0.01", message = "Flat electricity rate must be greater than 0")
    double flatElectricityRate;

    @Column(nullable = false)
    @DecimalMin(value = "0.01", message = "Flat cold water rate must be greater than 0")
    double flatColdWaterRate;

    @Column(nullable = false)
    @DecimalMin(value = "0.01", message = "Flat hot water rate must be greater than 0")
    double flatHotWaterRate;

    @Column(nullable = false)
    @DecimalMin(value = "0.01", message = "Flat heating rate must be greater than 0")
    double flatHeatingRate;

    @Column(nullable = false)
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
