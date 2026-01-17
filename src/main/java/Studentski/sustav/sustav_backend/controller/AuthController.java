package Studentski.sustav.sustav_backend.controller;

import Studentski.sustav.sustav_backend.auth.JWTUtil;
import Studentski.sustav.sustav_backend.models.Role;
import Studentski.sustav.sustav_backend.models.User;
import Studentski.sustav.sustav_backend.repositories.RoleRepository;
import Studentski.sustav.sustav_backend.repositories.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autentifikacija")
public class AuthController {

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleRepository roleRepository;

    @Operation(summary = "Registracija korisnika", description = "Dodaj novog korisnika u sustav")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Korisnik uspješno registriran", content = @Content()),
            @ApiResponse(responseCode = "400", description = "Neispravan zahtjev", content = @Content())
    })
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {

        // Hashiranje lozinke
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Dodavanje ROLE_USER iz baze
        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default ROLE_USER not found"));
        user.getRoles().clear();  // briše sve eventualne role koje user šalje
        user.getRoles().add(defaultRole);

        // Spremanje usera u bazu
        User savedUser = userRepository.save(user);

        return ResponseEntity.ok(savedUser);
    }


    @Operation(summary = "Prijava korisnika", description = "Prijava korisnika i dohvat tokena")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Korisnik uspješno prijavljen", content = @Content()),
            @ApiResponse(responseCode = "400", description = "Neispravan zahtjev", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Neovlašten pristup", content = @Content())
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestParam String email, @RequestParam String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        String accessToken = jwtUtil.generateToken(user.getEmail(), roles);
        String refreshToken = jwtUtil.generateRefreshToken();
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken
        ));
    }

    @Operation(summary = "Refresh token", description = "Generiranje novog access tokena koristeći refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access token uspješno generiran", content = @Content()),
            @ApiResponse(responseCode = "400", description = "Neispravan zahtjev", content = @Content()),
            @ApiResponse(responseCode = "403", description = "Neovlašten pristup", content = @Content())
    })
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestParam String refreshToken) {
        User user = userRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        String newAccessToken = jwtUtil.generateToken(user.getEmail(), roles);
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }
}
