package pl.wiktor.koprowski.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.wiktor.koprowski.domain.Apartment;

public interface ApartmentRepository extends JpaRepository<Apartment, Long> {
}
