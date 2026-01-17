package Studentski.sustav.sustav_backend.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="roles")
public class Role {

    @Schema(example = "1", description = "1=ROLE_USER, 2=ROLE_ADMIN")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Schema(hidden = true)
    @Column(unique = true, nullable = false)
    private String name;

    @ManyToMany(mappedBy = "roles")
    @JsonIgnore
    private Set<User> users = new HashSet<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Set<User> getUsers() { return users; }
    public void setUsers(Set<User> users) { this.users = users; }
}
