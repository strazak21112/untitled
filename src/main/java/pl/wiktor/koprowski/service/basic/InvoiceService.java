package pl.wiktor.koprowski.service.basic;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wiktor.koprowski.DTO.basic.InvoiceCreateDTO;
import pl.wiktor.koprowski.DTO.basic.InvoiceDTO;
import pl.wiktor.koprowski.DTO.row.InvoiceRowDTO;
import pl.wiktor.koprowski.domain.*;
import pl.wiktor.koprowski.repository.ApartmentRepository;
import pl.wiktor.koprowski.repository.InvoiceRepository;
import pl.wiktor.koprowski.repository.UserRepository;
import pl.wiktor.koprowski.repository.ReadingRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final ReadingRepository readingRepository;
    private final ApartmentRepository apartmentRepository;


    @Transactional
    public Invoice createInvoice(InvoiceCreateDTO invoiceDTO, String managerEmail) {
        Apartment apartment = apartmentRepository.findById(invoiceDTO.getApartmentId())
                .orElseThrow(() -> new IllegalArgumentException("error_apartment_not_found"));

        LocalDate issueDate = LocalDate.parse(invoiceDTO.getIssueDate());

         LocalDate billingStartDate = issueDate.withDayOfMonth(1);
        LocalDate billingEndDate = issueDate.withDayOfMonth(issueDate.lengthOfMonth());

        boolean exists = invoiceRepository.existsByApartmentAndBillingStartDateAndBillingEndDate(
                apartment,
                billingStartDate,
                billingEndDate
        );
        if (exists) {
            throw new IllegalArgumentException("error_invoice_already_exists");
        }

        Building building = apartment.getBuilding();

        Reading reading = apartment.getReadings().stream()
                .filter(r -> r.getBillingStartDate().equals(billingStartDate) &&
                        r.getBillingEndDate().equals(billingEndDate))
                .findFirst()
                .orElse(null);

        Invoice invoice = new Invoice();

         invoice.setRentAmount(round2(apartment.getArea() * building.getRentRatePerM2()));
        invoice.setOtherCharges(round2(apartment.getArea() * building.getOtherChargesPerM2()));
        invoice.setIssueDate(issueDate);
        invoice.setTenant(apartment.getTenant());
        invoice.setApartment(apartment);
        invoice.setPaid(false);
        invoice.setBillingStartDate(billingStartDate);
        invoice.setBillingEndDate(billingEndDate);
        invoice.setConfirmed(false);

        double totalMediaAmount;
        if (reading != null) {
            totalMediaAmount = (reading.getColdWaterValue() * building.getColdWaterRate()) +
                    (reading.getHotWaterValue() * building.getHotWaterRate()) +
                    (reading.getElectricityValue() * building.getElectricityRate()) +
                    (reading.getHeatingValue() * building.getHeatingRate());
            invoice.setReading(reading);
            invoice.setFlat(false);
        } else {
            totalMediaAmount = building.getFlatColdWaterRate() +
                    building.getFlatHotWaterRate() +
                    building.getFlatElectricityRate() +
                    building.getFlatHeatingRate();
            invoice.setFlat(true);
        }

        invoice.setTotalMediaAmount(round2(totalMediaAmount));
        invoice.setTotalAmount(round2(invoice.getRentAmount() + invoice.getOtherCharges() + totalMediaAmount));

        InvoiceInfo info = new InvoiceInfo();
        info.setAddress(building.getAddress());
        info.setElectricityRate(building.getElectricityRate());
        info.setColdWaterRate(building.getColdWaterRate());
        info.setHotWaterRate(building.getHotWaterRate());
        info.setHeatingRate(building.getHeatingRate());
        info.setRentRatePerM2(building.getRentRatePerM2());
        info.setOtherChargesPerM2(building.getOtherChargesPerM2());
        info.setFlatElectricityRate(building.getFlatElectricityRate());
        info.setFlatColdWaterRate(building.getFlatColdWaterRate());
        info.setFlatHotWaterRate(building.getFlatHotWaterRate());
        info.setFlatHeatingRate(building.getFlatHeatingRate());
        info.setApartmentNumber(apartment.getNumber());
        info.setApartmentFloor(apartment.getFloor());
        info.setApartmentArea(apartment.getArea());

        if (apartment.getTenant() != null) {
            User tenant = apartment.getTenant();
            info.setTenantFirstName(tenant.getFirstName());
            info.setTenantLastName(tenant.getLastName());
            info.setTenantPesel(tenant.getPesel().getPesel());
            info.setTenantEmail(tenant.getEmail());
            info.setTenantTelephone(tenant.getTelephone());
        }

        if (managerEmail != null && !managerEmail.isBlank()) {
            User manager = userRepository.findByEmail(managerEmail)
                    .orElseThrow(() -> new IllegalArgumentException("error_manager_not_found"));
            info.setManagerFirstName(manager.getFirstName());
            info.setManagerLastName(manager.getLastName());
            info.setManagerPesel(manager.getPesel().getPesel());
            info.setManagerEmail(manager.getEmail());
            info.setManagerTelephone(manager.getTelephone());
        }

        if (reading != null) {
            info.setColdWaterValue(reading.getColdWaterValue());
            info.setHotWaterValue(reading.getHotWaterValue());
            info.setHeatingValue(reading.getHeatingValue());
            info.setElectricityValue(reading.getElectricityValue());
        } else {
            info.setColdWaterValue(0);
            info.setHotWaterValue(0);
            info.setHeatingValue(0);
            info.setElectricityValue(0);
        }

        invoice.setInfo(info);

        return invoiceRepository.save(invoice);
    }




    @Transactional
    public Invoice updateInvoice(Long invoiceId, boolean confirmed, String managerEmail) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("error_invoice_not_found"));

        invoice.setConfirmed(confirmed);

        if (managerEmail != null && !managerEmail.isBlank()) {
            User manager = userRepository.findByEmail(managerEmail)
                    .orElseThrow(() -> new IllegalArgumentException("error_manager_not_found"));

            InvoiceInfo info = invoice.getInfo();
            if (info == null) {
                info = new InvoiceInfo();
            }

            info.setManagerFirstName(manager.getFirstName());
            info.setManagerLastName(manager.getLastName());
            info.setManagerPesel(manager.getPesel().getPesel());
            info.setManagerEmail(manager.getEmail());
            info.setManagerTelephone(manager.getTelephone());

            invoice.setInfo(info);
        }

        return invoiceRepository.save(invoice);
    }



    @Transactional
    public void deleteInvoice(Long invoiceId) {
         Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("error_invoice_not_found"));

         Reading reading = invoice.getReading();
        if (reading != null) {
            reading.setInvoice(null);
            readingRepository.save(reading);
        }

         invoiceRepository.delete(invoice);
    }

    @Transactional
    public Invoice payInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException("error_invoice_not_found"));

        if (invoice.isPaid()) {
            throw new IllegalStateException("error_invoice_already_paid");
        }

        invoice.setPaid(true);
        return invoiceRepository.save(invoice);
    }



    @Transactional(readOnly = true)
    public InvoiceDTO getInvoiceDetails(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("error_invoice_not_found"));

        InvoiceDTO dto = new InvoiceDTO();
        dto.setId(invoice.getId());
        dto.setRentAmount(invoice.getRentAmount());
        dto.setOtherCharges(invoice.getOtherCharges());
        dto.setTotalMediaAmount(invoice.getTotalMediaAmount());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setIssueDate(invoice.getIssueDate().toString());
        dto.setPaid(invoice.isPaid());
        dto.setBillingStartDate(invoice.getBillingStartDate().toString());
        dto.setBillingEndDate(invoice.getBillingEndDate().toString());
        dto.setConfirmed(invoice.isConfirmed());
        dto.setFlat(invoice.isFlat());

         dto.setInfo(invoice.getInfo());

        return dto;
    }
    public List<InvoiceRowDTO> getAllInvoiceRowsByBuilding(Long buildingId) {
        return invoiceRepository.findAll().stream()
                .filter(invoice -> invoice.getApartment() != null &&
                        invoice.getApartment().getBuilding() != null &&
                        invoice.getApartment().getBuilding().getId().equals(buildingId))
                .map(invoice -> new InvoiceRowDTO(
                        invoice.getId(),
                        invoice.isPaid(),
                        invoice.getBillingStartDate().toString(),
                        invoice.getBillingEndDate().toString(),
                        invoice.getApartment().getNumber()
                ))
                .collect(Collectors.toList());
    }

    private double round2(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }


    @Transactional(readOnly = true)
    public List<InvoiceRowDTO> getAllInvoiceRowsByTenantEmail(String tenantEmail) {
        return invoiceRepository.findAll().stream()
                .filter(invoice -> invoice.getTenant() != null &&
                        tenantEmail.equalsIgnoreCase(invoice.getTenant().getEmail()) &&
                        invoice.isConfirmed())
                .map(invoice -> new InvoiceRowDTO(
                        invoice.getId(),
                        invoice.isPaid(),
                        invoice.getBillingStartDate().toString(),
                        invoice.getBillingEndDate().toString(),
                        invoice.getApartment() != null ? invoice.getApartment().getNumber() : null
                ))
                .collect(Collectors.toList());
    }





}
