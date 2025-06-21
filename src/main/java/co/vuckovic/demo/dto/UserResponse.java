package co.vuckovic.demo.dto;

import java.util.List;

public record UserResponse(
        String id,
        String username,
        String email,
        Boolean enabled,
        List<String> roles
) {}