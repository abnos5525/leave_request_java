package ir.isiran.project.dto;

import java.util.List;

public record UserDTO(
        String username,
        String password,   // Optional for update
        String firstName,
        String lastName,
        String email,
        boolean enabled,
        List<String> roleNames) { }