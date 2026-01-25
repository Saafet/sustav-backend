package Studentski.sustav.sustav_backend.repositories;

import Studentski.sustav.sustav_backend.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import Studentski.sustav.sustav_backend.models.User;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByMembersContaining(User user);
}
