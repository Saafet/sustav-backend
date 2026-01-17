// java
package Studentski.sustav.sustav_backend.repositories;

import Studentski.sustav.sustav_backend.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(String name);
}
