package co.vuckovic.demo.service;

import co.vuckovic.demo.dto.UserCreateRequest;
import co.vuckovic.demo.dto.UserResponse;
import co.vuckovic.demo.dto.UserUpdateRequest;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response.Status;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserManagementService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    public List<UserResponse> getAllUsers() {
        return getAllUsers(0, 100);
    }

    public List<UserResponse> getAllUsers(int first, int max) {
        return keycloak
                .realm(realm)
                .users()
                .list(first, max)
                .stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(String userId) {
        UserRepresentation user = keycloak
                .realm(realm)
                .users()
                .get(userId)
                .toRepresentation();
        return mapToUserResponse(user);
    }

    public String createUser(UserCreateRequest request) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setEnabled(true);
        user.setEmailVerified(true);

        CredentialRepresentation cred = new CredentialRepresentation();
        cred.setType(CredentialRepresentation.PASSWORD);
        cred.setValue(request.password());
        cred.setTemporary(false);
        user.setCredentials(Collections.singletonList(cred));

        Response resp = keycloak.realm(realm).users().create(user);
        try {
            if (resp.getStatus() != Status.CREATED.getStatusCode()) {
                String err = resp.readEntity(String.class);
                log.error("Keycloak createUser failed: HTTP {} — {}", resp.getStatus(), err);
                throw new RuntimeException("Failed to create user (see logs)");
            }
            String path   = resp.getLocation().getPath();
            String userId = path.substring(path.lastIndexOf('/') + 1);

            if (request.roles() != null && !request.roles().isEmpty()) {
                assignRoles(userId, request.roles());
            }
            log.info("Created user '{}' (ID={})", request.username(), userId);
            return userId;
        } finally {
            resp.close();
        }
    }

    public void updateUser(String userId, UserUpdateRequest request) {
        UserResource ur = keycloak.realm(realm).users().get(userId);
        UserRepresentation existing = ur.toRepresentation();

        if (request.username() != null) existing.setUsername(request.username());
        if (request.email()    != null) existing.setEmail(request.email());
        if (request.enabled()  != null) existing.setEnabled(request.enabled());

        ur.update(existing);
        log.info("Updated user '{}'", userId);

        if (request.roles() != null) {
            updateRoles(userId, request.roles());
            log.info("Updated roles for user '{}': {}", userId, request.roles());
        }
    }

    public void deleteUser(String userId) {
        keycloak.realm(realm).users().delete(userId);
        log.info("Deleted user '{}'", userId);
    }

    private void assignRoles(String userId, List<String> roles) {
        UsersResource users = keycloak.realm(realm).users();
        List<RoleRepresentation> reps = roles.stream()
                .map(rn -> keycloak.realm(realm).roles().get(rn).toRepresentation())
                .collect(Collectors.toList());
        users.get(userId).roles().realmLevel().add(reps);
    }

    private void updateRoles(String userId, List<String> newRoles) {
        UserResource ur = keycloak.realm(realm).users().get(userId);

        List<RoleRepresentation> currentlyAssigned = ur.roles().realmLevel().listAll();
        List<RoleRepresentation> toRemove = currentlyAssigned.stream()
                .filter(r -> newRoles.contains(r.getName()))
                .collect(Collectors.toList());
        if (!toRemove.isEmpty()) {
            ur.roles().realmLevel().remove(toRemove);
        }

        List<RoleRepresentation> allRoles = keycloak.realm(realm).roles().list();
        List<RoleRepresentation> toAdd = newRoles.stream()
                .filter(name -> currentlyAssigned.stream().noneMatch(r -> r.getName().equals(name)))
                .map(name -> allRoles.stream()
                        .filter(r -> r.getName().equals(name))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Role not found: " + name)))
                .collect(Collectors.toList());
        if (!toAdd.isEmpty()) {
            ur.roles().realmLevel().add(toAdd);
        }
    }

    private UserResponse mapToUserResponse(UserRepresentation user) {
        List<String> roles = keycloak.realm(realm)
                .users()
                .get(user.getId())
                .roles()
                .realmLevel()
                .listAll()
                .stream()
                .map(RoleRepresentation::getName)
                .collect(Collectors.toList());

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isEnabled(),
                roles
        );
    }
}