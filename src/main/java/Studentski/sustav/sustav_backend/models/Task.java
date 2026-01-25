package Studentski.sustav.sustav_backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

@Entity
@Table(name = "tasks")
@Schema(description = "Zadatak unutar projekta")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false)
    @Schema(example = "Implementirati login")
    private String title;

    @Column(length = 1000)
    @Schema(example = "Napraviti JWT autentikaciju")
    private String description;

    @Schema(example = "TODO | IN_PROGRESS | DONE")
    private String status;

    // ==== Veza sa projektom ====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "tasks", "members"})
    private Project project;

    // ==== ASSIGNED USER (POSTOJI U BAZI, ALI SE NE VIDI U API-JU) ====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_user_id")
    @JsonIgnore   // ⬅️ KLJUCNA STVAR
    @Schema(hidden = true) // ⬅️ da Swagger ne prikazuje
    private User assignedTo;

    // ===== GETTERS & SETTERS =====
    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public User getAssignedTo() { return assignedTo; }
    public void setAssignedTo(User assignedTo) { this.assignedTo = assignedTo; }
}
