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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
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
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Dohvati sve korisnike")
    @GetMapping("/all")
    public Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Operation(summary = "Pretraži korisnike po imenu ili emailu")
    @GetMapping("/search")
    public List<User> searchUsers(@RequestParam String query) {
        return userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
    }


    // ======================
    // Promjena role
    // =====================

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
    public ResponseEntity<?> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Lozinka resetovana"));
    }
}
