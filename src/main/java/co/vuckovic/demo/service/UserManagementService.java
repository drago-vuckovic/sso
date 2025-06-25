package co.vuckovic.demo.service;

import co.vuckovic.demo.config.KeycloakProperties;
import co.vuckovic.demo.dto.UserCreateRequest;
import co.vuckovic.demo.dto.UserResponse;
import co.vuckovic.demo.dto.UserSummary;
import co.vuckovic.demo.dto.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

import static org.springframework.http.HttpMethod.GET;

@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final WebClient keycloakWebClient;
    private final KeycloakProperties props;

    public Flux<UserSummary> getAllUsers() {
        return keycloakWebClient.get()
                .uri("/admin/realms/{realm}/users", props.realm())
                .retrieve()
                .bodyToFlux(UserRep.class)
                .map(u -> new UserSummary(u.id(), u.username()));
    }

    public Mono<UserResponse> getUserById(String userId) {
        return keycloakWebClient.get()
                .uri("/admin/realms/{realm}/users/{id}", props.realm(), userId)
                .retrieve()
                .bodyToMono(UserRep.class)
                .flatMap(u -> fetchRolesForUser(userId)
                        .collectList()
                        .map(roles -> new UserResponse(u.id(), u.username(), u.email(), u.enabled(), roles))
                );
    }

    public Mono<String> createUser(UserCreateRequest req) {
        return keycloakWebClient.post()
                .uri("/admin/realms/{realm}/users", props.realm())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "username", req.username(),
                        "email", req.email(),
                        "enabled", true,
                        "credentials", List.of(Map.of(
                                "type", "password",
                                "value", req.password(),
                                "temporary", false
                        ))
                ))
                .exchangeToMono(resp -> {
                    if (resp.statusCode().is2xxSuccessful()) {
                        return Mono.justOrEmpty(resp.headers()
                                .header("Location")
                                .stream()
                                .findFirst()
                                .map(loc -> loc.substring(loc.lastIndexOf('/') + 1)));
                    } else {
                        return resp.createException().flatMap(Mono::error);
                    }
                });
    }

    public Mono<Void> updateUser(String userId, UserUpdateRequest req) {
        return keycloakWebClient.get()
                .uri("/admin/realms/{realm}/users/{id}", props.realm(), userId)
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(body -> {
                    if (req.username() != null) body.put("username", req.username());
                    if (req.email() != null) body.put("email", req.email());
                    if (req.enabled() != null) body.put("enabled", req.enabled());

                    body.putIfAbsent("emailVerified", true); // optional but sometimes required
                    body.putIfAbsent("attributes", new HashMap<>()); // required in some setups

                    Mono<Void> updateMono = keycloakWebClient.put()
                            .uri("/admin/realms/{realm}/users/{id}", props.realm(), userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(body)
                            .exchangeToMono(resp -> {
                                if (resp.statusCode().is2xxSuccessful()) {
                                    return Mono.empty();
                                } else {
                                    return resp.bodyToMono(String.class)
                                            .flatMap(error -> Mono.error(new RuntimeException("Update failed: " + error)));
                                }
                            });

                    if (req.roles() != null) {
                        return updateMono.then(updateUserRoles(userId, req.roles()));
                    } else {
                        return updateMono;
                    }
                });
    }

    public Mono<Void> deleteUser(String userId) {
        return keycloakWebClient.delete()
                .uri("/admin/realms/{realm}/users/{id}", props.realm(), userId)
                .retrieve()
                .bodyToMono(Void.class);
    }

    private Flux<String> fetchRolesForUser(String userId) {
        return keycloakWebClient.get()
                .uri("/admin/realms/{realm}/users/{id}/role-mappings/realm/composite", props.realm(), userId)
                .retrieve()
                .bodyToFlux(Map.class)
                .map(m -> (String) m.get("name"));
    }

    private Mono<Void> updateUserRoles(String userId, List<String> newRoles) {
        ParameterizedTypeReference<Map<String, Object>> type = new ParameterizedTypeReference<>() {};

        Mono<List<Map<String, Object>>> repsMono = keycloakWebClient.get()
                .uri("/admin/realms/{realm}/roles", props.realm())
                .retrieve()
                .bodyToFlux(type)
                .filter(m -> newRoles.contains(m.get("name")))
                .collectList();

        Mono<Void> deleteMono = keycloakWebClient.delete()
                .uri("/admin/realms/{realm}/users/{id}/role-mappings/realm", props.realm(), userId)
                .retrieve()
                .bodyToMono(Void.class);

        Mono<Void> addMono = repsMono.flatMapMany(reps -> keycloakWebClient.post()
                        .uri("/admin/realms/{realm}/users/{id}/role-mappings/realm", props.realm(), userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(reps)
                        .retrieve()
                        .bodyToFlux(Void.class))
                .then();

        return deleteMono.then(addMono);
    }

    private static record UserRep(String id, String username, String email, Boolean enabled) {}
}
