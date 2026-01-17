package Studentski.sustav.sustav_backend.repositories;

import Studentski.sustav.sustav_backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    java.util.Optional<User> findByEmail(String email);
    java.util.Optional<User> findByRefreshToken(String refreshToken);
}