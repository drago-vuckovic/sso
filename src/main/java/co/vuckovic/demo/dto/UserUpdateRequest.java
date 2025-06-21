package co.vuckovic.demo.dto;

import java.util.List;

public record UserUpdateRequest(
        String username,
        String email,
        Boolean enabled,
        List<String> roles
) {}
