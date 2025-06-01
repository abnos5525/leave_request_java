package ir.isiran.project.service;

import ir.isiran.project.dto.UserDTO;
import ir.isiran.project.mapper.UserMapper;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;

import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    @Value("${keycloak.realm}")
    private String authRealm;

    private final Keycloak keycloak;

    private UsersResource getUsersResource() {
        return keycloak.realm(authRealm).users();
    }

    public List<UserRepresentation> getAll() {
        return getUsersResource().list();
    }

    public void create(UserDTO userDto) {
        UserRepresentation user = UserMapper.toRepresentation(userDto);
        Response response = getUsersResource().create(user);

        if (response.getStatus() != 201) {
            String error = response.readEntity(String.class);
            throw new RuntimeException("User creation failed: " + error);
        }
    }

    public UserRepresentation get(String userId) {
        UserRepresentation user = getUsersResource().get(userId).toRepresentation();
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        return user;
    }

    public void update(String userId, UserDTO userDto) {
        UserRepresentation existing = get(userId);
        UserRepresentation updated = UserMapper.toRepresentation(userDto);
        updated.setId(existing.getId());

        getUsersResource().get(userId).update(updated);
    }

    public void delete(String userId) {
        getUsersResource().delete(userId);
    }
}