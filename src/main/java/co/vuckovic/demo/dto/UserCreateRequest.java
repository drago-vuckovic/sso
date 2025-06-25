package co.vuckovic.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "UserCreateRequest", description = "Payload to create a new user")
public record UserCreateRequest(

        @Schema(description = "Unique username", example = "alice")
        String username,

        @Schema(description = "User email address", example = "alice@example.com")
        String email,

        @Schema(description = "Initial password", example = "P@ssw0rd!", format = "password")
        String password,

        @Schema(description = "List of realm roles to assign", example = "[\"USER\",\"EDITOR\"]")
        List<String> roles
) {}