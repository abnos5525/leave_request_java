package ir.isiran.project.service;

import ir.isiran.project.dto.RoleDTO;
import ir.isiran.project.mapper.RoleMapper;
import javax.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    @Value("${keycloak.realm}")
    private String authRealm;

    private final Keycloak keycloak;
    private final RoleMapper roleMapper;

    public List<RoleRepresentation> getAll() {
        return keycloak.realm(authRealm).roles().list();
    }

    public void create(RoleDTO roleDTO) {
        RoleRepresentation role = roleMapper.toRepresentation(roleDTO);
        keycloak.realm(authRealm).roles().create(role);
    }

    public RoleRepresentation get(String roleId) {
        List<RoleRepresentation> roles = getAll();
        return roles.stream()
                .filter(role -> roleId.equals(role.getId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Role not found"));
    }

    public void update(String id, RoleDTO roleDTO) {
        RoleRepresentation updatedRole = roleMapper.toRepresentation(roleDTO);
        RoleRepresentation existingRole = get(id);

        updatedRole.setId(existingRole.getId());

        RoleResource roleResource = keycloak.realm(authRealm).roles().get(existingRole.getName());

        roleResource.update(updatedRole);
    }

    public void delete(String id) {
        RoleRepresentation role = get(id);
        String roleName = role.getName();

        keycloak.realm(authRealm).roles().deleteRole(roleName);
    }
}