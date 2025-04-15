package pl.wiktor.koprowski.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.wiktor.koprowski.domain.ActivationToken;

import java.util.Optional;

public interface ActivationTokenRepository extends JpaRepository<ActivationToken, Long> {
    Optional<ActivationToken> findByToken(String token);
    boolean existsByToken(String token);


}
