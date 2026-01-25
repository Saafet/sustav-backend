package Studentski.sustav.sustav_backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
@Schema(description = "Projekt koji sadrži zadatke i članove")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Column(nullable = false)
    @Schema(example = "Web aplikacija za evidenciju zadataka")
    private String name;

    @Column(length = 1000)
    @Schema(example = "Projekt iz kolegija Programsko inzenjerstvo")
    private String description;

    // ==== Članovi projekta ====
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "project_users",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnoreProperties({"projects", "tasks"})
    private Set<User> members = new HashSet<>();

    // ==== Zadatci projekta ====
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"project", "assignedTo"})
    private Set<Task> tasks = new HashSet<>();

    // ===== GETTERS & SETTERS =====
    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Set<User> getMembers() { return members; }
    public void setMembers(Set<User> members) { this.members = members; }

    public Set<Task> getTasks() { return tasks; }
    public void setTasks(Set<Task> tasks) { this.tasks = tasks; }

    // ===== POMOĆNE METODE =====
    @JsonIgnore
    public void removeAllMembers() {
        for (User u : members) {
            u.getProjects().remove(this); // ukloni projekt iz korisnika
        }
        members.clear();
    }
}
