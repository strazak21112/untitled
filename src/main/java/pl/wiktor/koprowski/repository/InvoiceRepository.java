package pl.wiktor.koprowski.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.wiktor.koprowski.domain.Apartment;
import pl.wiktor.koprowski.domain.Invoice;
import pl.wiktor.koprowski.domain.User;

import java.time.LocalDate;
import java.util.Set;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

     Set<Invoice> findByTenant(User tenant);
    boolean existsByApartmentAndBillingStartDateAndBillingEndDate(Apartment apartment, LocalDate billingStartDate, LocalDate billingEndDate);

}
