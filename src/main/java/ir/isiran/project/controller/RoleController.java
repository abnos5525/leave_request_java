package ir.isiran.project.controller;

import ir.isiran.project.dto.RoleDTO;
import ir.isiran.project.service.RoleService;

import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public List<RoleRepresentation> getAll() {
        return roleService.getAll();
    }

    @GetMapping("/{id}")
    public RoleRepresentation get(@PathVariable String id) {
        return roleService.get(id);
    }

    @PostMapping
    public void create(@RequestBody RoleDTO roleDTO) {
        roleService.create(roleDTO);
    }

    @PutMapping("/{id}")
    public void updateRole(@PathVariable String id, @RequestBody RoleDTO roleDTO) {
        roleService.update(id, roleDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteRole(@PathVariable String id) {
        roleService.delete(id);
    }
}