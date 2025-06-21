package co.vuckovic.demo.dto;

import java.util.List;

public record UserCreateRequest(
        String username,
        String email,
        String password,
        List<String> roles
) {}