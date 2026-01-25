package Studentski.sustav.sustav_backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="users")
public class User {

    @Schema(hidden = true)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(example = "Ime korisnika")
    @Column(nullable = false)
    private String name;

    @Schema(example = "Prezime korisnika")
    @Column(nullable = false)
    private String lastname;

    @Schema(example = "user@example.com")
    @Column(unique = true, nullable = false)
    private String email;

    @Schema(example = "Lozinka123")
    @Column(nullable = false)
    private String password;

    // 🔐 REFRESH TOKEN
    @Schema(hidden = true)
    @JsonIgnore
    @Column(length = 500)
    private String refreshToken;

    // ⏰ ISTEK REFRESH TOKENA
    @Schema(hidden = true)
    @JsonIgnore
    @Column
    private LocalDateTime refreshTokenExpiry;

    @Schema(hidden = true)
    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name="user_id"),
            inverseJoinColumns = @JoinColumn(name="role_id")
    )
    private Set<Role> roles = new HashSet<>();

    // =========================
    // NOVO: ManyToMany veza s projektima
    // =========================
    @JsonIgnore
    @ManyToMany(mappedBy = "members", fetch = FetchType.EAGER)
    private Set<Project> projects = new HashSet<>();

    public User() {}

    public User(Long id, String name, String lastname, String email, String password) {
        this.id = id;
        this.name = name;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
    }

    // ===== GETTERI & SETTERI =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public LocalDateTime getRefreshTokenExpiry() { return refreshTokenExpiry; }
    public void setRefreshTokenExpiry(LocalDateTime refreshTokenExpiry) { this.refreshTokenExpiry = refreshTokenExpiry; }

    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }

    public Set<Project> getProjects() { return projects; }
    public void setProjects(Set<Project> projects) { this.projects = projects; }

    @Schema(hidden = true)
    @JsonIgnore
    public String getRoleName() {
        if (roles.isEmpty()) return "USER";
        return roles.iterator().next().getName();
    }

    public void setRoleName(Role role) {
        this.roles.clear();
        this.roles.add(role);
    }

    public void setRoleName(String roleName) {
        this.roles.clear();
        Role role = new Role();
        role.setName(roleName.toUpperCase());
        this.roles.add(role);
    }
}
