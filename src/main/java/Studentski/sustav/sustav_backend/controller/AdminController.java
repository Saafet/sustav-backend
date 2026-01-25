package Studentski.sustav.sustav_backend.controller;

import Studentski.sustav.sustav_backend.models.User;
import Studentski.sustav.sustav_backend.models.Role;
import Studentski.sustav.sustav_backend.repositories.RoleRepository;
import Studentski.sustav.sustav_backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "3. Upravljanje admin panelom")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Operation(summary = "Dohvati korisnika po ID-u")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Korisnik pronađen"),
            @ApiResponse(responseCode = "401", description = "Neautorizovan pristup"),
            @ApiResponse(responseCode = "403", description = "Zabranjen pristup"),
            @ApiResponse(responseCode = "404", description = "Korisnik nije pronađen")
    })
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Dohvati sve korisnike")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista korisnika"),
            @ApiResponse(responseCode = "401", description = "Neautorizovan pristup"),
            @ApiResponse(responseCode = "403", description = "Zabranjen pristup")
    })
    @GetMapping("/all")
    public Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Operation(summary = "Pretraži korisnike po imenu ili emailu")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rezultati pretrage"),
            @ApiResponse(responseCode = "401", description = "Neautorizovan pristup"),
            @ApiResponse(responseCode = "403", description = "Zabranjen pristup")
    })
    @GetMapping("/search")
    public List<User> searchUsers(@RequestParam String query) {
        return userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
    }

    @Operation(summary = "Kreiraj novog korisnika")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Korisnik uspješno kreiran"),
            @ApiResponse(responseCode = "400", description = "Korisnik već postoji"),
            @ApiResponse(responseCode = "401", description = "Neautorizovan pristup"),
            @ApiResponse(responseCode = "403", description = "Zabranjen pristup")
    })
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

        Role role = roleRepository.findByName("ROLE_" + roleName.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        newUser.getRoles().add(role);
        userRepository.save(newUser);

        return ResponseEntity.ok(Map.of("message", "Korisnik kreiran"));
    }

    @Operation(summary = "Uredi korisnika")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Korisnik ažuriran"),
            @ApiResponse(responseCode = "401", description = "Neautorizovan pristup"),
            @ApiResponse(responseCode = "403", description = "Zabranjen pristup"),
            @ApiResponse(responseCode = "404", description = "Korisnik nije pronađen")
    })
    @PutMapping("/edit/{id}")
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

    @Operation(summary = "Obriši korisnika")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Korisnik obrisan"),
            @ApiResponse(responseCode = "401", description = "Neautorizovan pristup"),
            @ApiResponse(responseCode = "403", description = "Zabranjen pristup"),
            @ApiResponse(responseCode = "404", description = "Korisnik nije pronađen")
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Korisnik obrisan"));
    }

    @Operation(summary = "Promijeni rolu korisniku po ID-u")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rola uspješno promijenjena"),
            @ApiResponse(responseCode = "401", description = "Neautorizovan pristup"),
            @ApiResponse(responseCode = "403", description = "Zabranjen pristup"),
            @ApiResponse(responseCode = "404", description = "Korisnik ili rola nisu pronađeni")
    })
    @PutMapping("/role-by-id")
    public ResponseEntity<?> changeRoleById(@RequestParam Long userId,
                                            @RequestParam Long roleId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role nije pronađena"));

        user.getRoles().clear();
        user.getRoles().add(role);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "Rola korisniku uspješno promijenjena na: " + role.getName()
        ));
    }

    @Operation(summary = "Reset lozinke korisnika")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lozinka resetovana"),
            @ApiResponse(responseCode = "401", description = "Neautorizovan pristup"),
            @ApiResponse(responseCode = "403", description = "Zabranjen pristup"),
            @ApiResponse(responseCode = "404", description = "Korisnik nije pronađen")
    })
    @PutMapping("/reset-password/{id}")
    public ResponseEntity<?> resetPassword(@PathVariable Long id,
                                           @RequestParam String newPassword) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Lozinka resetovana"));
    }

    @Operation(summary = "Obriši više korisnika odjednom")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Bulk brisanje završeno"),
            @ApiResponse(responseCode = "401", description = "Neautorizovan pristup"),
            @ApiResponse(responseCode = "403", description = "Zabranjen pristup")
    })
    @DeleteMapping("/bulk-delete")
    public ResponseEntity<?> bulkDelete(@RequestParam List<Long> ids) {
        ids.forEach(id -> {
            if (userRepository.existsById(id)) {
                userRepository.deleteById(id);
            }
        });
        return ResponseEntity.ok(Map.of("message", "Bulk brisanje završeno"));
    }
}
