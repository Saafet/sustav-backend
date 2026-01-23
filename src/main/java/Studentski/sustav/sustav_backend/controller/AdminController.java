package Studentski.sustav.sustav_backend.controller;

import Studentski.sustav.sustav_backend.models.User;
import Studentski.sustav.sustav_backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Upravljanje admin panelom")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Operation(
            summary = "Dohvati admin panel",
            description = "Preuzmi informacije i pristup admin panelu za autentificiranog korisnika s admin pravima",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pristup admin panelu uspješan",
                    content = @Content()),
            @ApiResponse(responseCode = "400", description = "Neispravan zahtjev",
                    content = @Content()),
            @ApiResponse(responseCode = "403", description = "Neovlašten pristup. Pristup je odbijen",
                    content = @Content())
    })
//    @GetMapping("/panel")
//    public ResponseEntity<Map<String, String>> adminPanel() {
//        return ResponseEntity.ok(Map.of("message", "pristup adminu odobren"));
//    }
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    public Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }
}
