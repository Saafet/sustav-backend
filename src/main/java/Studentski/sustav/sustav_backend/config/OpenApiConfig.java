package Studentski.sustav.sustav_backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Studentski sustav za upravljanje projektima i zadatcima")
                        .version("2.0")
                        .description(
                                "Baza podataka za upravljanje studentskim projektima i zadatcima "
                                        + "sa autentifikacijom putem JWT tokena."
                        )
                )
                .tags(List.of(
                        new Tag()
                                .name("1. Autentifikacija")
                                .description("API za login, registraciju i refresh token"),
                        new Tag()
                                .name("2. Upravljanje korisnicima")
                                .description("API za korisnicki profil i osnovne korisnicke funkcionalnosti"),
                        new Tag()
                                .name("3. Upravljanje admin panelom")
                                .description("Admin rute za upravljanje korisnicima i rolama"),
                        new Tag()
                                .name("4. Projekti")
                                .description("Operacije nad studentskim projektima"),
                        new Tag()
                                .name("5. Zadaci")
                                .description("Operacije nad zadacima u sklopu projekata")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(
                                securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                );
    }
}
