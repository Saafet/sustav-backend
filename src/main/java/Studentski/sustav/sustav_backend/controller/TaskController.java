package Studentski.sustav.sustav_backend.controller;

import Studentski.sustav.sustav_backend.models.Project;
import Studentski.sustav.sustav_backend.models.Task;
import Studentski.sustav.sustav_backend.models.User;
import Studentski.sustav.sustav_backend.repositories.ProjectRepository;
import Studentski.sustav.sustav_backend.repositories.TaskRepository;
import Studentski.sustav.sustav_backend.repositories.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Zadaci")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    // 🔹 Dohvati zadatke po projektu
    @Operation(summary = "Dohvati zadatke po projektu")
    @GetMapping("/project/{projectId}")
    public List<Task> getTasksByProject(@PathVariable Long projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    // 🔹 Dohvati zadatke po korisniku (student vidi svoje zadatke)
    @Operation(summary = "Dohvati zadatke po korisniku")
    @GetMapping("/user/{userId}")
    public List<Task> getTasksByUser(@PathVariable Long userId) {
        return taskRepository.findByAssignedToId(userId);
    }

    // 🔹 Kreiraj zadatak (profesor/admin)
    @Operation(summary = "Kreiraj novi zadatak")
    @PostMapping
    public Task createTask(
            @RequestParam Long projectId,
            @RequestParam Long userId,
            @RequestBody Task task
    ) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Projekt ne postoji"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik ne postoji"));

        task.setProject(project);
        task.setAssignedTo(user);
        task.setStatus("TODO");

        return taskRepository.save(task);
    }

    // 🔹 Update statusa zadatka (student može samo svoj, profesor može sve)
    @Operation(summary = "Promijeni status zadatka")
    @PatchMapping("/{taskId}/status")
    public Task updateTaskStatus(
            @PathVariable Long taskId,
            @RequestParam String status,
            @RequestParam Long userId
    ) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Zadatak ne postoji"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik ne postoji"));

        // Ako je student, može mijenjati samo svoj zadatak
        boolean isStudent = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_STUDENT"));

        if (isStudent && !task.getAssignedTo().getId().equals(userId)) {
            throw new RuntimeException("Nemate pravo mijenjati ovaj zadatak");
        }

        task.setStatus(status);
        return taskRepository.save(task);
    }

    // 🔹 Brisanje zadatka (samo profesor/admin)
    @Operation(summary = "Obriši zadatak (samo profesor)")
    @DeleteMapping("/{taskId}")
    public ResponseEntity<String> deleteTask(
            @PathVariable Long taskId,
            @RequestParam Long userId
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik ne postoji"));

        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            return ResponseEntity
                    .status(403)
                    .body("Nemate pravo brisati zadatak");
        }

        taskRepository.deleteById(taskId);
        return ResponseEntity
                .ok("Zadatak je uspješno obrisan!");
    }

}
