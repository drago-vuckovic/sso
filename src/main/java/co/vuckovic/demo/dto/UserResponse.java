package co.vuckovic.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "UserResponse", description = "Detailed info about a user")
public record UserResponse(
        @Schema(description = "User unique ID", example = "715da428-8464-4d74-bb69-a4718bc05882")
        String id,

        @Schema(description = "Username", example = "alice")
        String username,

        @Schema(description = "Email address", example = "alice@example.com")
        String email,

        @Schema(description = "Account enabled flag", example = "true")
        Boolean enabled,

        @Schema(description = "Assigned realm roles", example = "[\"USER\",\"EDITOR\"]")
        List<String> roles
) {}