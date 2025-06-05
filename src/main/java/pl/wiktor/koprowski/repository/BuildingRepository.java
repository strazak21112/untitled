package pl.wiktor.koprowski.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wiktor.koprowski.domain.Address;
import pl.wiktor.koprowski.domain.Building;

import java.util.Optional;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {
    boolean existsByAddress(Address address);

}
