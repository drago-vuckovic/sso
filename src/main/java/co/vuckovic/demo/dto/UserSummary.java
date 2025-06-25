package co.vuckovic.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserSummary", description = "Brief info about a user")
public record UserSummary(
        @Schema(description = "User unique ID", example = "715da428-8464-4d74-bb69-a4718bc05882")
        String id,
        @Schema(description = "Username", example = "alice")
        String username) {}
