package pl.wiktor.koprowski.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.wiktor.koprowski.domain.Apartment;
import pl.wiktor.koprowski.domain.Invoice;
import pl.wiktor.koprowski.domain.Reading;

import java.time.LocalDate;
import java.util.Optional;

public interface ReadingRepository extends JpaRepository<Reading, Long> {

    boolean existsByApartmentAndBillingStartDateAndBillingEndDate(
            Apartment apartment, LocalDate billingStartDate, LocalDate billingEndDate);

    Optional<Reading> findByInvoice(Invoice invoice);
}
