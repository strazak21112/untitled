package pl.wiktor.koprowski.service.basic;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wiktor.koprowski.DTO.basic.ReadingDTO;
import pl.wiktor.koprowski.DTO.inside.ReadingDetails;
import pl.wiktor.koprowski.DTO.row.ApartmentRowDTO;
import pl.wiktor.koprowski.DTO.row.ReadingRowDTO;
import pl.wiktor.koprowski.domain.*;
        import pl.wiktor.koprowski.repository.InvoiceRepository;
import pl.wiktor.koprowski.repository.ReadingRepository;
import pl.wiktor.koprowski.repository.ApartmentRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReadingService {

    private final ReadingRepository readingRepository;
    private final ApartmentRepository apartmentRepository;
    private final InvoiceRepository invoiceRepository;

    @Transactional
    public Reading createReading(ReadingDTO readingDTO) {
        Apartment apartment = apartmentRepository.findById(readingDTO.getApartmentId())
                .orElseThrow(() -> new RuntimeException("error_apartment_not_found"));

        if (readingDTO.getDate() == null || readingDTO.getDate().isEmpty()) {
            throw new RuntimeException("error_reading_date_required");
        }

        LocalDate date = LocalDate.parse(readingDTO.getDate());

        LocalDate billingStartDate = date.withDayOfMonth(1);
        LocalDate billingEndDate = date.with(TemporalAdjusters.lastDayOfMonth());

        boolean readingExists = readingRepository.existsByApartmentAndBillingStartDateAndBillingEndDate(
                apartment,
                billingStartDate,
                billingEndDate
        );
        if (readingExists) {
            throw new RuntimeException("error_reading_already_exists");
        }

        Reading reading = new Reading();
        reading.setColdWaterValue(readingDTO.getColdWaterValue());
        reading.setHotWaterValue(readingDTO.getHotWaterValue());
        reading.setHeatingValue(readingDTO.getHeatingValue());
        reading.setElectricityValue(readingDTO.getElectricityValue());
        reading.setDate(date);
        reading.setBillingStartDate(billingStartDate);
        reading.setBillingEndDate(billingEndDate);
        reading.setApartment(apartment);

        Reading savedReading = readingRepository.save(reading);

        Optional<Invoice> optionalInvoice = invoiceRepository.findByApartmentAndBillingStartDateAndBillingEndDate(
                apartment, billingStartDate, billingEndDate
        );

        if (optionalInvoice.isPresent()) {
            Invoice invoice = optionalInvoice.get();
            if (invoice.isConfirmed()) {
                throw new RuntimeException("error_invoice_already_confirmed");
            }

            Building building = apartment.getBuilding();

            invoice.setReading(savedReading);
            invoice.setFlat(false);

            double totalMediaAmount = round2(
                    (savedReading.getColdWaterValue() * building.getColdWaterRate())
                            + (savedReading.getHotWaterValue() * building.getHotWaterRate())
                            + (savedReading.getElectricityValue() * building.getElectricityRate())
                            + (savedReading.getHeatingValue() * building.getHeatingRate())
            );

            invoice.setTotalMediaAmount(totalMediaAmount);
            invoice.setTotalAmount(round2(totalMediaAmount + invoice.getRentAmount() + invoice.getOtherCharges()));

            InvoiceInfo info = invoice.getInfo();
            info.setColdWaterValue(savedReading.getColdWaterValue());
            info.setHotWaterValue(savedReading.getHotWaterValue());
            info.setHeatingValue(savedReading.getHeatingValue());
            info.setElectricityValue(savedReading.getElectricityValue());
            invoice.setInfo(info);

            invoiceRepository.save(invoice);
        }

        return savedReading;
    }

    @Transactional
    public Reading updateReading(Long id, ReadingDTO readingDTO) {
        Reading reading = readingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("error_reading_not_found"));

        Invoice invoice = reading.getInvoice();
        if (invoice != null && invoice.isConfirmed()) {
            throw new RuntimeException("error_reading_update_invoice_confirmed");
        }

        reading.setColdWaterValue(readingDTO.getColdWaterValue());
        reading.setHotWaterValue(readingDTO.getHotWaterValue());
        reading.setHeatingValue(readingDTO.getHeatingValue());
        reading.setElectricityValue(readingDTO.getElectricityValue());

        if (invoice != null) {
            Building building = reading.getApartment().getBuilding();

            double totalMediaAmount = round2(
                    (reading.getColdWaterValue() * building.getColdWaterRate())
                            + (reading.getHotWaterValue() * building.getHotWaterRate())
                            + (reading.getElectricityValue() * building.getElectricityRate())
                            + (reading.getHeatingValue() * building.getHeatingRate())
            );

            invoice.setTotalMediaAmount(totalMediaAmount);
            invoice.setTotalAmount(round2(totalMediaAmount + invoice.getRentAmount() + invoice.getOtherCharges()));

            InvoiceInfo info = invoice.getInfo();
            info.setColdWaterValue(reading.getColdWaterValue());
            info.setHotWaterValue(reading.getHotWaterValue());
            info.setHeatingValue(reading.getHeatingValue());
            info.setElectricityValue(reading.getElectricityValue());

            invoiceRepository.save(invoice);
        }

        return readingRepository.save(reading);
    }

    @Transactional
    public void deleteReading(Long id) {
        Reading reading = readingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("error_reading_not_found"));

        Invoice invoice = reading.getInvoice();
        if (invoice != null) {
            if (!invoice.isConfirmed()) {
                Building building = invoice.getApartment().getBuilding();

                double totalMediaAmount = round2(
                        building.getFlatColdWaterRate()
                                + building.getFlatHotWaterRate()
                                + building.getFlatElectricityRate()
                                + building.getFlatHeatingRate()
                );

                invoice.setReading(null);
                invoice.setFlat(true);
                invoice.setTotalMediaAmount(totalMediaAmount);
                invoice.setTotalAmount(round2(totalMediaAmount + invoice.getRentAmount() + invoice.getOtherCharges()));

                InvoiceInfo info = invoice.getInfo();
                info.setColdWaterValue(0);
                info.setHotWaterValue(0);
                info.setHeatingValue(0);
                info.setElectricityValue(0);

                invoice.setInfo(info);
            } else {
                invoice.setReading(null);
            }
            invoiceRepository.save(invoice);
        }

        Apartment apartment = reading.getApartment();
        if (apartment != null) {
            apartment.getReadings().remove(reading);
            apartmentRepository.save(apartment);
        }

        readingRepository.delete(reading);
    }

    public List<ReadingRowDTO> getAllReadingRows() {
        return readingRepository.findAll().stream()
                .map(reading -> new ReadingRowDTO(
                        reading.getId(),
                        reading.getBillingStartDate().toString(),
                        reading.getBillingEndDate().toString(),
                        reading.getApartment().getBuilding().getAddress(),
                        reading.getApartment().getNumber()
                ))
                .collect(Collectors.toList());
    }

    public ReadingDetails getReadingDetails(Long id) {
        Reading reading = readingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("error_reading_not_found"));

        Apartment apartment = reading.getApartment();

        ApartmentRowDTO apartmentRow = new ApartmentRowDTO(
                apartment.getId(),
                apartment.getBuilding().getAddress(),
                apartment.getNumber()
        );

        return new ReadingDetails(
                reading.getId(),
                reading.getColdWaterValue(),
                reading.getHotWaterValue(),
                reading.getHeatingValue(),
                reading.getElectricityValue(),
                reading.getDate().toString(),
                reading.getBillingStartDate().toString(),
                reading.getBillingEndDate().toString(),
                apartmentRow
        );
    }

    private double round2(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }


    @Transactional(readOnly = true)
    public List<ReadingRowDTO> getReadingRowsByTenantEmail(String tenantEmail) {
        return readingRepository.findAll().stream()
                .filter(reading -> reading.getInvoice() != null
                        && reading.getInvoice().getTenant() != null
                        && tenantEmail.equalsIgnoreCase(reading.getInvoice().getTenant().getEmail())
                        && reading.getApartment() != null
                        && reading.getApartment().getBuilding() != null)
                .map(reading -> new ReadingRowDTO(
                        reading.getId(),
                        reading.getBillingStartDate().toString(),
                        reading.getBillingEndDate().toString(),
                        reading.getApartment().getBuilding().getAddress(),
                        reading.getApartment().getNumber()
                ))
                .collect(Collectors.toList());
    }





}
