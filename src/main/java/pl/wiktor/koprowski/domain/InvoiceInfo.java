package pl.wiktor.koprowski.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class InvoiceInfo {

    private Address address;

     private double electricityRate;
    private double coldWaterRate;
    private double hotWaterRate;
    private double heatingRate;

    private double rentRatePerM2;
    private double otherChargesPerM2;

    private double flatElectricityRate;
    private double flatColdWaterRate;
    private double flatHotWaterRate;
    private double flatHeatingRate;

    private String apartmentNumber;
    private int apartmentFloor;
    private double apartmentArea;

     private String tenantFirstName;
    private String tenantLastName;
    private String tenantPesel;
    private String tenantEmail;
    private String tenantTelephone;

    private String managerFirstName;
    private String managerLastName;
    private String managerPesel;
    private String managerEmail;
    private String managerTelephone;

    private double coldWaterValue;
    private double hotWaterValue;
    private double heatingValue;
    private double electricityValue;

}
