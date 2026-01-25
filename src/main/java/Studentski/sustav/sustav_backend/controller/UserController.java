package Studentski.sustav.sustav_backend.controller;

import Studentski.sustav.sustav_backend.models.User;
import Studentski.sustav.sustav_backend.repositories.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Tag(name = "2. Upravljanje korisnicima")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Operation(summary = "Dohvati trenutno prijavljenog korisnika")
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronadjen"));

        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Izmjena imena i prezimena korisnika")
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestParam String name,
            @RequestParam String lastname
    ) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronadjen"));

        user.setName(name);
        user.setLastname(lastname);

        userRepository.save(user);

        return ResponseEntity.ok(
                Map.of("message", "Profil uspjesno azuriran")
        );
    }

    @Operation(summary = "Promjena lozinke")
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword
    ) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronadjen"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Pogresna stara lozinka")
            );
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(
                Map.of("message", "Lozinka uspjesno promijenjena")
        );
    }

    @Operation(summary = "Logout korisnika")
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronadjen"));

        user.setRefreshToken(null);
        user.setRefreshTokenExpiry(null);
        userRepository.save(user);

        SecurityContextHolder.clearContext();

        return ResponseEntity.ok(
                Map.of("message", "Uspješan logout")
        );
    }

}
