package Studentski.sustav.sustav_backend.controller;

import Studentski.sustav.sustav_backend.models.User;
import Studentski.sustav.sustav_backend.repositories.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@Tag(name = "Upravljanje korisnicima")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Operation(
            summary = "Dohvati profil korisnika",
            description = "Preuzmi informacije profila za autentificiranog korisnika"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profil korisnika uspjesno dohvacen",
                    content = @Content()),
            @ApiResponse(responseCode = "403", description = "Neovlasten pristup",
                    content = @Content())
    })
    //@GetMapping("/profile")
    //return ResponseEntity.ok(
                //Map.of("message", "pristup korisniku odobren")
        //);
    //}

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
}
