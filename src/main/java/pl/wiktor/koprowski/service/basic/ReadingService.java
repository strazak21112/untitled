package pl.wiktor.koprowski.service.basic;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.wiktor.koprowski.DTO.basic.ReadingDTO;
import pl.wiktor.koprowski.domain.Invoice;
import pl.wiktor.koprowski.domain.Reading;
import pl.wiktor.koprowski.domain.Apartment;
import pl.wiktor.koprowski.repository.InvoiceRepository;
import pl.wiktor.koprowski.repository.ReadingRepository;
import pl.wiktor.koprowski.repository.ApartmentRepository;

import java.util.List;
import java.util.Optional;

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

        boolean exists = readingRepository.existsByApartmentAndBillingStartDateAndBillingEndDate(
                apartment,
                readingDTO.getBillingStartDate(),
                readingDTO.getBillingEndDate()
        );
        if (exists) {
            throw new RuntimeException("error_reading_already_exists");
        }

        Reading reading = new Reading();
        reading.setColdWaterValue(readingDTO.getColdWaterValue());
        reading.setHotWaterValue(readingDTO.getHotWaterValue());
        reading.setHeatingValue(readingDTO.getHeatingValue());
        reading.setElectricityValue(readingDTO.getElectricityValue());
        reading.setDate(readingDTO.getDate());
        reading.setBillingStartDate(readingDTO.getBillingStartDate());
        reading.setBillingEndDate(readingDTO.getBillingEndDate());
        reading.setApartment(apartment);

        return readingRepository.save(reading);
    }

    @Transactional
    public Reading updateReading(Long id, ReadingDTO readingDTO, String lang) {
        lang = lang.toLowerCase();


        String finalLang = lang;
        Reading reading = readingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        finalLang.equals("pl") ? "Odczyt nie znaleziony" :
                                finalLang.equals("de") ? "Ablesung nicht gefunden" :
                                        "Reading not found"
                ));

        Invoice invoice = reading.getInvoice();
        if (invoice != null && invoice.isConfirmed()) {
            throw new IllegalStateException(
                    lang.equals("pl") ? "Nie można edytować odczytu powiązanego z potwierdzoną fakturą" :
                            lang.equals("de") ? "Ablesung kann nicht bearbeitet werden, da sie mit einer bestätigten Rechnung verknüpft ist" :
                                    "Cannot update reading linked to a confirmed invoice"
            );
        }



        reading.setColdWaterValue(readingDTO.getColdWaterValue());
        reading.setHotWaterValue(readingDTO.getHotWaterValue());
        reading.setHeatingValue(readingDTO.getHeatingValue());
        reading.setElectricityValue(readingDTO.getElectricityValue());


        return readingRepository.save(reading);
    }


    @Transactional
    public void deleteReading(Long id, String lang) {
        lang = lang.toLowerCase();

        if (!lang.equals("pl") && !lang.equals("en") && !lang.equals("de")) {
            throw new IllegalArgumentException(
                    lang.equals("pl") ? "Nieprawidłowy język" :
                            lang.equals("de") ? "Ungültige Sprache" :
                                    "Invalid language"
            );
        }

        String finalLang = lang;
        Reading reading = readingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        finalLang.equals("pl") ? "Odczyt nie znaleziony" :
                                finalLang.equals("de") ? "Ablesung nicht gefunden" :
                                        "Reading not found"
                ));

         Invoice invoice = reading.getInvoice();
        if (invoice != null) {
            invoice.setReading(null);
            invoiceRepository.save(invoice);
        }

         Apartment apartment = reading.getApartment();
        if (apartment != null) {
            apartment.getReadings().remove(reading);
            apartmentRepository.save(apartment);
        }

         readingRepository.delete(reading);
    }

}
