package pl.wiktor.koprowski.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.wiktor.koprowski.domain.Building;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {
}
