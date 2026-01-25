package Studentski.sustav.sustav_backend.controller;

import Studentski.sustav.sustav_backend.models.Project;
import Studentski.sustav.sustav_backend.models.User;
import Studentski.sustav.sustav_backend.repositories.ProjectRepository;
import Studentski.sustav.sustav_backend.repositories.UserRepository;
import Studentski.sustav.sustav_backend.dto.ProjectCreateDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Projekti", description = "Operacije nad projektima")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "Dohvati sve projekte")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista projekata dohvaćena"),
            @ApiResponse(responseCode = "401", description = "Korisnik nije autoriziran")
    })
    @GetMapping
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    @Operation(summary = "Dohvati projekt po ID-u")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projekt dohvaćen"),
            @ApiResponse(responseCode = "404", description = "Projekt ne postoji"),
            @ApiResponse(responseCode = "401", description = "Korisnik nije autoriziran")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        return projectRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Kreiraj novi projekt")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projekt kreiran"),
            @ApiResponse(responseCode = "400", description = "Neispravan zahtjev"),
            @ApiResponse(responseCode = "401", description = "Korisnik nije autoriziran")
    })
    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody ProjectCreateDTO dto) {
        try {
            Project project = new Project();
            project.setName(dto.getName());
            project.setDescription(dto.getDescription());

            // Members po ID-u
            Set<User> members = new HashSet<>();
            if (dto.getMembers() != null) {
                for (Long userId : dto.getMembers()) {
                    userRepository.findById(userId).ifPresent(members::add);
                }
            }
            project.setMembers(members);

            // Ovdje više **ne dodajemo taskove**, projekt je čist
            project.setTasks(new HashSet<>());

            Project saved = projectRepository.save(project);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @Operation(summary = "Dodaj korisnika na projekt")
    @PostMapping("/{projectId}/add-user/{userId}")
    public ResponseEntity<Project> addUserToProject(
            @PathVariable Long projectId,
            @PathVariable Long userId
    ) {
        Project project = projectRepository.findById(projectId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);

        if (project == null || user == null) {
            return ResponseEntity.badRequest().build();
        }

        project.getMembers().add(user);
        return ResponseEntity.ok(projectRepository.save(project));
    }

    @Operation(summary = "Ukloni korisnika iz projekta")
    @DeleteMapping("/{projectId}/remove-user/{userId}")
    public ResponseEntity<Project> removeUserFromProject(
            @PathVariable Long projectId,
            @PathVariable Long userId
    ) {
        Project project = projectRepository.findById(projectId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);

        if (project == null || user == null) {
            return ResponseEntity.badRequest().build();
        }

        project.getMembers().remove(user);
        return ResponseEntity.ok(projectRepository.save(project));
    }

    @Operation(summary = "Dohvati sve članove projekta")
    @GetMapping("/{projectId}/members")
    public ResponseEntity<Set<User>> getProjectMembers(@PathVariable Long projectId) {
        return projectRepository.findById(projectId)
                .map(p -> ResponseEntity.ok(p.getMembers()))
                .orElse(ResponseEntity.notFound().build());
    }

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
}
