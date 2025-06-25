package co.vuckovic.demo.controller;

import co.vuckovic.demo.dto.UserCreateRequest;
import co.vuckovic.demo.dto.UserResponse;
import co.vuckovic.demo.dto.UserSummary;
import co.vuckovic.demo.dto.UserUpdateRequest;
import co.vuckovic.demo.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserManagementService userManagementService;

    @GetMapping
    public Flux<UserSummary> getAllUsers() {
        return userManagementService.getAllUsers();
    }

    @GetMapping("/{userId}")
    public Mono<UserResponse> getUserById(@PathVariable String userId) {
        return userManagementService.getUserById(userId);
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> createUser(@RequestBody UserCreateRequest request) {
        return userManagementService.createUser(request);
    }


    @PutMapping("/{userId}")
    public void updateUser(@PathVariable String userId, @RequestBody UserUpdateRequest request) {
        userManagementService.updateUser(userId, request);
    }


    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable String userId) {
        userManagementService.deleteUser(userId);
    }

}