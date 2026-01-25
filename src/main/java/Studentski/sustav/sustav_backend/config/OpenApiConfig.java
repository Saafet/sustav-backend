// java
package Studentski.sustav.sustav_backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Studentski sustav za upravljanje projektima i zadatcima")
                        .version("2.0")
                        .description("Databaza za upravljanje studentskim projektima i zadatcima s autentifikacijom putem JWT tokena."))
                // Redoslijed tagova određuje redoslijed prikaza u swagger
                .tags(
                        java.util.List.of(
                                new Tag().name("Autentifikacija").description("API za login i registraciju"),
                                new Tag().name("Upravljanje korisnicima").description("API za korisničke funkcionalnosti"),
                                new Tag().name("Upravljanje admin panelom").description("API za admin funkcije")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
