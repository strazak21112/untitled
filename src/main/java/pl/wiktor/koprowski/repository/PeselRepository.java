package pl.wiktor.koprowski.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.wiktor.koprowski.domain.Pesel;

public interface PeselRepository extends JpaRepository<Pesel, Long> {
     boolean existsByPesel(String pesel);
}
