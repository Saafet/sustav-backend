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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "5. Zadaci")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;


    // ADMIN RUTE


    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Kreiraj novi zadatak (samo admin)")
    @PostMapping
    public ResponseEntity<?> createTask(
            @RequestParam Long projectId,
            @RequestParam Long userId,
            @RequestBody Task task
    ) {
        Project project = projectRepository.findById(projectId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);

        if (project == null || user == null) {
            return ResponseEntity.badRequest().body("Projekt ili korisnik ne postoji");
        }

        task.setProject(project);
        task.setAssignedTo(user);
        task.setStatus("TODO");

        Task savedTask = taskRepository.save(task);
        return ResponseEntity.ok(savedTask);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obriši zadatak (samo admin)")
    @DeleteMapping("/{taskId}")
    public ResponseEntity<String> deleteTask(@PathVariable Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            return ResponseEntity.notFound().build();
        }
        taskRepository.deleteById(taskId);
        return ResponseEntity.ok("Zadatak je uspješno obrisan!");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Dohvati sve zadatke po projektu (admin vidi sve)")
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<Task>> getTasksByProjectAdmin(@PathVariable Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            return ResponseEntity.notFound().build();
        }
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        return ResponseEntity.ok(tasks);
    }


    // USER RUTE


    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Dohvati zadatke prijavljenog korisnika")
    @GetMapping("/my-tasks")
    public ResponseEntity<List<Task>> getMyTasks(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        List<Task> tasks = taskRepository.findByAssignedToId(user.getId());
        return ResponseEntity.ok(tasks);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Promijeni status zadatka (user može samo svoj, admin sve)")
    @PatchMapping("/{taskId}/status")
    public ResponseEntity<?> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestParam String status,
            Authentication authentication
    ) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) {
            return ResponseEntity.status(404).body("Zadatak ne postoji");
        }

        User user = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).body("Korisnik ne postoji");
        }

        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        if (!isAdmin && !task.getAssignedTo().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Nemate pravo mijenjati ovaj zadatak");
        }

        task.setStatus(status);
        Task updatedTask = taskRepository.save(task);
        return ResponseEntity.ok(updatedTask);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Dohvati zadatke po projektu u kojima je korisnik član")
    @GetMapping("/project/my/{projectId}")
    public ResponseEntity<List<Task>> getTasksByProjectUser(
            @PathVariable Long projectId,
            Authentication authentication
    ) {
        User user = userRepository.findByEmail(authentication.getName()).orElse(null);
        if (user == null || !projectRepository.existsById(projectId)) {
            return ResponseEntity.notFound().build();
        }

        List<Task> tasks = taskRepository.findByProjectId(projectId);
        tasks.removeIf(t -> t.getAssignedTo() == null || !t.getAssignedTo().getId().equals(user.getId()));
        return ResponseEntity.ok(tasks);
    }
}
