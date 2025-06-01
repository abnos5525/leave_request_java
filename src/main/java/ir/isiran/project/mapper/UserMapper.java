package ir.isiran.project.mapper;

import ir.isiran.project.dto.UserDTO;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

public class UserMapper {

    public static UserRepresentation toRepresentation(UserDTO dto) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setEnabled(dto.enabled());

        if (dto.password() != null && !dto.password().isEmpty()) {
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(dto.password());
            user.setCredentials(List.of(credential));
        }

        return user;
    }

    public static UserDTO toDTO(UserRepresentation user) {
        return new UserDTO(
                user.getUsername(),
                null,  // Omit password in DTO
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.isEnabled(),
                user.getRealmRoles()
        );
    }
}