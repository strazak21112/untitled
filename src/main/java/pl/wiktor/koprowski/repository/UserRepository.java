package pl.wiktor.koprowski.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.wiktor.koprowski.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByTelephone(String telephone);

}
