package Studentski.sustav.sustav_backend.controller;

import Studentski.sustav.sustav_backend.models.User;
import Studentski.sustav.sustav_backend.models.Role;
import Studentski.sustav.sustav_backend.repositories.RoleRepository;
import Studentski.sustav.sustav_backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Upravljanje admin panelom")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Operation(summary = "Dohvati korisnika po ID-u")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Dohvati sve korisnike")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Operation(summary = "Pretraži korisnike po imenu ili emailu")
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> searchUsers(@RequestParam String query) {
        return userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
    }

    // ======================
    // Kreiranje korisnika
    // ======================
    @Autowired
    private RoleRepository roleRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestParam String email,
                                        @RequestParam String password,
                                        @RequestParam String name,
                                        @RequestParam String lastname,
                                        @RequestParam(defaultValue = "USER") String roleName) {

        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Korisnik već postoji"));
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setName(name);
        newUser.setLastname(lastname);

        // Dohvati role iz baze
        Role role = roleRepository.findByName("ROLE_" + roleName.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Role not found"));
        newUser.getRoles().add(role);

        userRepository.save(newUser);

        return ResponseEntity.ok(Map.of("message", "Korisnik kreiran"));
    }




    // ======================
    // Uređivanje korisnika
    // ======================
    @Operation(summary = "Uredi korisnika")
    @PutMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> editUser(@PathVariable Long id,
                                      @RequestParam(required = false) String name,
                                      @RequestParam(required = false) String lastname,
                                      @RequestParam(required = false) String email) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        if (name != null) user.setName(name);
        if (lastname != null) user.setLastname(lastname);
        if (email != null) user.setEmail(email);

        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Korisnik uspješno ažuriran"));
    }

    // ======================
    // Promjena role

    @Operation(summary = "Promijeni rolu korisniku po ID-u")
    @PutMapping("/role-by-id")
    public ResponseEntity<?> changeRoleById(
            @RequestParam Long userId,
            @RequestParam Long roleId) {

        // 1. Dohvati korisnika
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        // 2. Dohvati rolu po ID-u
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role nije pronađena"));

        // 3. Dodaj rolu korisniku
        user.getRoles().clear();
        user.getRoles().add(role);

        // 4. Spremi promjene
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Rola korisniku uspješno promijenjena na: " + role.getName()));
    }



    // ======================
    // Reset lozinke
    // ======================
    @Operation(summary = "Reset lozinke korisnika")
    @PutMapping("/reset-password/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Lozinka resetovana"));
    }

}
