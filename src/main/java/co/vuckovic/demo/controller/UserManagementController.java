package co.vuckovic.demo.controller;

import co.vuckovic.demo.dto.UserCreateRequest;
import co.vuckovic.demo.dto.UserResponse;
import co.vuckovic.demo.dto.UserSummary;
import co.vuckovic.demo.dto.UserUpdateRequest;
import co.vuckovic.demo.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Tag(name = "admin", description = "Admin-only user management operations")
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userManagementService;

    @Operation(
            summary = "List all users",
            description = "Returns summaries for all users in the realm",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of users returned"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    @GetMapping
    public Flux<UserSummary> getAllUsers() {
        return userManagementService.getAllUsers();
    }

    @Operation(
            summary = "Get a user by ID",
            description = "Fetch detailed information for one user",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "User found"),
                    @ApiResponse(responseCode = "404", description = "User not found"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    @GetMapping("/{userId}")
    public Mono<UserResponse> getUserById(@PathVariable String userId) {
        return userManagementService.getUserById(userId);
    }

    @Operation(
            summary = "Create a new user",
            description = "Adds a new user with specified roles and credentials",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "User created"),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> createUser(@RequestBody UserCreateRequest request) {
        return userManagementService.createUser(request);
    }

    @Operation(
            summary = "Update an existing user",
            description = "Modifies username, email, enabled flag or roles",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Update successful"),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @PutMapping("/{userId}")
    public void updateUser(@PathVariable String userId, @RequestBody UserUpdateRequest request) {
        userManagementService.updateUser(userId, request);
    }

    @Operation(
            summary = "Delete a user",
            description = "Removes a user by ID",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "User deleted"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable String userId) {
        userManagementService.deleteUser(userId);
    }

}