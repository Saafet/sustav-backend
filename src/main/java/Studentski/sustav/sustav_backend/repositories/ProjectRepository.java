package Studentski.sustav.sustav_backend.repositories;

import Studentski.sustav.sustav_backend.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
}
