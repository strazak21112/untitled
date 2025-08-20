package pl.wiktor.koprowski.service.basic;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wiktor.koprowski.DTO.basic.InvoiceDTO;
import pl.wiktor.koprowski.domain.*;
import pl.wiktor.koprowski.repository.ApartmentRepository;
import pl.wiktor.koprowski.repository.InvoiceRepository;
import pl.wiktor.koprowski.repository.UserRepository;
import pl.wiktor.koprowski.repository.ReadingRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final ReadingRepository readingRepository;
    private final ApartmentRepository apartmentRepository;


    @Transactional
    public Invoice createInvoice(InvoiceDTO invoiceDTO, String lang) {
        lang = lang.toLowerCase();

        // Sprawdzamy, czy użytkownik istnieje
        String finalLang = lang;
        User tenant = userRepository.findById(invoiceDTO.getUserId())
                .orElseThrow(() -> new IllegalArgumentException(
                        finalLang.equals("pl") ? "Użytkownik nie znaleziony" :
                                finalLang.equals("de") ? "Benutzer nicht gefunden" :
                                        "User not found"
                ));

        // Sprawdzamy, czy mieszkanie istnieje
        String finalLang1 = lang;
        Apartment apartment = apartmentRepository.findById(invoiceDTO.getApartmentId())
                .orElseThrow(() -> new IllegalArgumentException(
                        finalLang1.equals("pl") ? "Mieszkanie nie znalezione" :
                                finalLang1.equals("de") ? "Wohnung nicht gefunden" :
                                        "Apartment not found"
                ));

        // Sprawdzamy, czy w tym samym okresie rozliczeniowym dla tego mieszkania istnieje już faktura
        boolean exists = invoiceRepository.existsByApartmentAndBillingStartDateAndBillingEndDate(
                apartment,
                invoiceDTO.getBillingStartDate(),
                invoiceDTO.getBillingEndDate()
        );
        if (exists) {
            throw new IllegalArgumentException(
                    lang.equals("pl") ? "Faktura dla tego mieszkania w tym okresie już istnieje" :
                            lang.equals("de") ? "Für diese Wohnung existiert bereits eine Rechnung in diesem Zeitraum" :
                                    "An invoice for this apartment already exists in this period"
            );
        }

        Building building = apartment.getBuilding();
        Reading reading = null;
        if (invoiceDTO.getReadingId() != null) {
            reading = readingRepository.findById(invoiceDTO.getReadingId()).orElse(null);
        }
        Invoice invoice = new Invoice();
        invoice.setRentAmount(apartment.getArea() * building.getRentRatePerM2());
        invoice.setOtherCharges(apartment.getArea() * building.getOtherChargesPerM2());
        invoice.setIssueDate(invoiceDTO.getIssueDate());
        invoice.setTenant(tenant);
        invoice.setApartment(apartment);
        invoice.setPaid(invoiceDTO.isPaid());
        invoice.setBillingStartDate(invoiceDTO.getBillingStartDate());
        invoice.setBillingEndDate(invoiceDTO.getBillingEndDate());
        invoice.setConfirmed(invoiceDTO.isConfirmed());

        double totalMediaAmount;
        if (reading != null) {
            totalMediaAmount = (reading.getColdWaterValue() * building.getColdWaterRate()) +
                    (reading.getHotWaterValue() * building.getHotWaterRate()) +
                    (reading.getElectricityValue() * building.getElectricityRate()) +
                    (reading.getHeatingValue() * building.getHeatingRate());
        } else {
            totalMediaAmount = building.getFlatColdWaterRate() +
                    building.getFlatHotWaterRate() +
                    building.getFlatElectricityRate() +
                    building.getFlatHeatingRate();
        }

        invoice.setTotalMediaAmount(totalMediaAmount);
        invoice.setTotalAmount(totalMediaAmount + invoice.getRentAmount() + invoice.getOtherCharges());



        // Powiązanie z odczytem, jeśli istnieje
        if (reading != null) {
            invoice.setReading(reading);
        }

        // Zapisujemy fakturę w bazie danych
        return invoiceRepository.save(invoice);
    }


    public Optional<Invoice> getInvoiceById(Long id) {
        return invoiceRepository.findById(id);
    }


    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }


    @Transactional
    public Invoice updateInvoice(Long invoiceId, boolean confirmed, String lang) {
        lang = lang.toLowerCase();

        // Sprawdzamy, czy faktura istnieje
        String finalLang = lang;
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException(
                        finalLang.equals("pl") ? "Faktura nie znaleziona" :
                                finalLang.equals("de") ? "Rechnung nicht gefunden" :
                                        "Invoice not found"
                ));

        // Aktualizujemy tylko pole 'confirmed'
        invoice.setConfirmed(confirmed);

        // Zapisujemy zmodyfikowaną fakturę
        return invoiceRepository.save(invoice);
    }


    @Transactional
    public void deleteInvoice(Long invoiceId, String lang) {
        lang = lang.toLowerCase();

        // Sprawdzamy, czy faktura istnieje
        String finalLang = lang;
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new IllegalArgumentException(
                        finalLang.equals("pl") ? "Faktura nie znaleziona" :
                                finalLang.equals("de") ? "Rechnung nicht gefunden" :
                                        "Invoice not found"
                ));

        if (invoice.isConfirmed()) {
            throw new IllegalStateException(
                    finalLang.equals("pl") ? "Faktura jest potwierdzona" :
                            finalLang.equals("de") ? "Rechnung ist bestätigt" :
                                    "Invoice is  confirmed"
            );
        }        Reading reading = invoice.getReading();
        if (reading != null) {
            reading.setInvoice(null);
            readingRepository.save(reading); // Zapisujemy zmodyfikowany obiekt reading
        }

        // Usuwamy fakturę
        invoiceRepository.delete(invoice);
    }

}
