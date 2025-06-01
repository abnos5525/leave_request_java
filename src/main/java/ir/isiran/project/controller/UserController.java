package ir.isiran.project.controller;

import ir.isiran.project.dto.UserDTO;
import ir.isiran.project.service.UserService;

import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserRepresentation> getAll() {
        return userService.getAll();
    }

    @PostMapping
    public void create(@RequestBody UserDTO userDTO) {
        userService.create(userDTO);
    }

    @GetMapping("/{id}")
    public UserRepresentation getById(@PathVariable String id) {
        return userService.get(id);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable String id, @RequestBody UserDTO userDTO) {
        userService.update(id, userDTO);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        userService.delete(id);
    }
}