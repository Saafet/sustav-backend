package Studentski.sustav.sustav_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Tag(name = "Upravljanje korisnicima")
public class UserController {

    @Operation(
            summary = "Dohvati profil korisnika",
            description = "Preuzmi informacije profila za autentificiranog korisnika",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profil korisnika uspješno dohvaćen",
                    content = @Content()),
            @ApiResponse(responseCode = "400", description = "Neispravan zahtjev",
                    content = @Content()),
            @ApiResponse(responseCode = "403", description = "Neovlašten pristup. Pristup je odbijen",
                    content = @Content())
    })
    @GetMapping("/profile")
    public ResponseEntity<Map<String, String>> userProfile() {
        return ResponseEntity.ok(Map.of("message", "pristup korisniku odobren"));
    }
}
