package Studentski.sustav.sustav_backend.controller;

import Studentski.sustav.sustav_backend.models.Project;
import Studentski.sustav.sustav_backend.models.User;
import Studentski.sustav.sustav_backend.repositories.ProjectRepository;
import Studentski.sustav.sustav_backend.repositories.UserRepository;
import Studentski.sustav.sustav_backend.dto.ProjectCreateDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "4. Projekti")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;


    // ADMIN RUTE


    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Dohvati sve projekte (admin vidi sve)")
    @GetMapping
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Kreiraj novi projekt")
    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody ProjectCreateDTO dto) {
        try {
            Project project = new Project();
            project.setName(dto.getName());
            project.setDescription(dto.getDescription());

            Set<User> members = new HashSet<>();
            // Ako DTO sadrži ID korisnika > 0, dodaj ga odmah
            if (dto.getMembers() != null) {
                for (Long userId : dto.getMembers()) {
                    if (userId > 0) {
                        userRepository.findById(userId).ifPresent(members::add);
                    }
                }
            }

            project.setMembers(members);
            project.setTasks(new HashSet<>());

            Project saved = projectRepository.save(project);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Dodaj korisnika na projekt")
    @PostMapping("/{projectId}/add-user/{userId}")
    public ResponseEntity<Project> addUserToProject(
            @PathVariable Long projectId,
            @PathVariable Long userId
    ) {
        Project project = projectRepository.findById(projectId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);

        if (project == null || user == null) return ResponseEntity.badRequest().build();

        project.getMembers().add(user);
        return ResponseEntity.ok(projectRepository.save(project));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Ukloni korisnika iz projekta")
    @DeleteMapping("/{projectId}/remove-user/{userId}")
    public ResponseEntity<Project> removeUserFromProject(
            @PathVariable Long projectId,
            @PathVariable Long userId
    ) {
        Project project = projectRepository.findById(projectId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);

        if (project == null || user == null) return ResponseEntity.badRequest().build();

        project.getMembers().remove(user);
        return ResponseEntity.ok(projectRepository.save(project));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obriši projekt")
    @DeleteMapping("/{projectId}/delete")
    public ResponseEntity<String> deleteProject(@PathVariable Long projectId) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) return ResponseEntity.status(404).body("Projekt ne postoji");

        try {
            for (User u : project.getMembers()) {
                u.getProjects().remove(project);
            }
            project.getMembers().clear();
            projectRepository.flush();
            projectRepository.delete(project);

            return ResponseEntity.ok("Projekt obrisan: " + project.getName());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Greška: " + e.getMessage());
        }
    }

    // KORISNIK RUTE

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Dohvati projekte u kojima je korisnik član")
    @GetMapping("/my-projects/{userId}")
    public ResponseEntity<List<Project>> getMyProjects(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.badRequest().build();

        List<Project> myProjects = projectRepository.findAll()
                .stream()
                .filter(p -> p.getMembers().contains(user))
                .collect(Collectors.toList());

        return ResponseEntity.ok(myProjects);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Dohvati slobodne projekte koji još nisu dodijeljeni nikome")
    @GetMapping("/available-projects")
    public List<Project> getAvailableProjects() {
        return projectRepository.findAll()
                .stream()
                .filter(p -> p.getMembers().isEmpty())
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(summary = "Korisnik preuzima slobodan projekt")
    @PostMapping("/take/{projectId}")
    public ResponseEntity<Project> takeProject(
            @PathVariable Long projectId,
            Authentication authentication
    ) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();

        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) return ResponseEntity.badRequest().build();

        if (!project.getMembers().isEmpty()) {
            return ResponseEntity.status(403).build();
        }

        project.getMembers().add(user);
        Project saved = projectRepository.save(project);
        return ResponseEntity.ok(saved);
    }

}
