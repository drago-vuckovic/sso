package co.vuckovic.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "UserUpdateRequest", description = "Fields to update on an existing user")
public record UserUpdateRequest(
        @Schema(description = "New username", example = "alice2")
        String username,

        @Schema(description = "New email address", example = "alice2@example.com")
        String email,

        @Schema(description = "Enable or disable the user")
        Boolean enabled,

        @Schema(description = "Updated list of realm roles", example = "[\"USER\",\"ADMIN\"]")
        List<String> roles
) {}
