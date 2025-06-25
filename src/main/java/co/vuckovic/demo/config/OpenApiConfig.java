package co.vuckovic.demo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.*;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        servers = {
                @Server(url = "http://localhost:8081", description = "Local")
        },
        info = @Info(
                title       = "Demo User Management API",
                version     = "1.0.0",
                description = "Reactive CRUD over Keycloak users",
                contact     = @Contact(name = "Drago Vuckovic", email = "drago@vuckovic.co")
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name             = "bearerAuth",
        type             = SecuritySchemeType.HTTP,
        scheme           = "bearer",
        bearerFormat     = "JWT",
        description      = "Provide your JWT token here"
)
@Configuration
public class OpenApiConfig {

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .pathsToMatch("/api/admin/**")
                .build();
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch("/api/users", "/api/users/**")
                .build();
    }
}

