package Studentski.sustav.sustav_backend.repositories;

import Studentski.sustav.sustav_backend.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Dohvati sve zadatke po projektu
    List<Task> findByProjectId(Long projectId);

    // Dohvati sve zadatke po korisniku (student vidi svoje zadatke)
    List<Task> findByAssignedToId(Long userId);
}
