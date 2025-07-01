package ir.isiran.project.service;

import ir.isiran.project.dto.RoleDTO;
import ir.isiran.project.mapper.RoleMapper;
import javax.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    @Value("${keycloak.realm}")
    private String authRealm;

    @Value("${keycloak.resource}")
    private String authClient;

    private final Keycloak keycloak;
    private final RoleMapper roleMapper;

    // Helper method to get client ID by client name
    private String getClientIdByName(String clientName) {
        List<ClientRepresentation> clients = keycloak.realm(authRealm).clients().findByClientId(clientName);
        if (clients.isEmpty()) {
            throw new NotFoundException("Client not found with name: " + clientName);
        }
        return clients.get(0).getId();
    }

    public List<RoleRepresentation> getAll() {
        String clientInternalId = getClientIdByName(authClient);
        return keycloak.realm(authRealm).clients().get(clientInternalId).roles().list();
    }

    public void create(RoleDTO roleDTO) {
        String clientInternalId = getClientIdByName(authClient);
        RoleRepresentation role = roleMapper.toRepresentation(roleDTO);
        keycloak.realm(authRealm).clients().get(clientInternalId).roles().create(role);
    }

    public RoleRepresentation get(String roleId) {
        String clientInternalId = getClientIdByName(authClient);
        List<RoleRepresentation> roles = keycloak.realm(authRealm).clients().get(clientInternalId).roles().list();
        return roles.stream()
                .filter(role -> roleId.equals(role.getId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Role not found"));
    }

    public void update(String id, RoleDTO roleDTO) {
        String clientInternalId = getClientIdByName(authClient);
        RoleRepresentation updatedRole = roleMapper.toRepresentation(roleDTO);
        RoleRepresentation existingRole = get(id);

        updatedRole.setId(existingRole.getId());
        RoleResource roleResource = keycloak.realm(authRealm)
                .clients()
                .get(clientInternalId)
                .roles()
                .get(existingRole.getName());

        roleResource.update(updatedRole);
    }

    public void delete(String id) {
        String clientInternalId = getClientIdByName(authClient);
        RoleRepresentation role = get(id);
        keycloak.realm(authRealm)
                .clients()
                .get(clientInternalId)
                .roles()
                .deleteRole(role.getName());
    }
}