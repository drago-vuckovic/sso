package co.vuckovic.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "public", description = "Endpoints accessible to ROLE_USER or above")
@RestController
@RequestMapping("/api")
public class UserController {

    @Operation(summary = "Get simple user list view",
            description = "Returns a placeholder string for user list",
            security = {})
    @GetMapping("/users")
    public String getUsers() {
        return "User list";
    }

    @Operation(summary = "Edit resource placeholder",
            description = "Endpoint requiring ROLE_EDITOR",
            security = {})
    @GetMapping("/edit")
    public String editResource() {
        return "Edit access";
    }

    @Operation(summary = "Admin resource placeholder",
            description = "Endpoint requiring ROLE_ADMIN or manage-users authority",
            security = {})
    @GetMapping("/admin")
    public String adminResource() {
        return "Admin access";
    }
}