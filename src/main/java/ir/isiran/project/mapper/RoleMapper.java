package ir.isiran.project.mapper;

import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Component;

import ir.isiran.project.dto.RoleDTO;

@Component
public class RoleMapper {

    public RoleDTO toDTO(RoleRepresentation role) {
        return new RoleDTO(role.getName(), role.getDescription());
    }

    public RoleRepresentation toRepresentation(RoleDTO roleDTO) {
        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleDTO.name());
        role.setDescription(roleDTO.description());
        return role;
    }
}