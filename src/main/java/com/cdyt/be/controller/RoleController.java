package com.cdyt.be.controller;

import com.cdyt.be.dto.role.CreateRoleDto;
import com.cdyt.be.entity.Role;
import com.cdyt.be.service.RoleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("api/roles")
@Tag(name = "Role", description = "APIs for managing roles")
public class RoleController {

  private final RoleService roleService;

  public RoleController(RoleService roleService) {
    this.roleService = roleService;
  }

  /**
   * Get all roles
   *
   * @return List of all roles
   */
  @GetMapping
  public ResponseEntity<List<Role>> findAll() {
    return ResponseEntity.ok(roleService.getAllRoles());
  }

  /**
   * Find a role by its ID
   *
   * @param id The ID of the role to find
   * @return The role if found, or a 404 Not Found response
   */
  @GetMapping("/{id}")
  public ResponseEntity<Role> findById(int id) {
    return roleService.getRoleById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Create a new role
   *
   * @param role The role to create
   * @return The created role
   */
  @PostMapping
  public ResponseEntity<Role> createRole(@RequestBody @Valid CreateRoleDto role) {
    Role savedRole = roleService.createRole(role);
    if (savedRole != null) {
      return ResponseEntity.ok(savedRole);
    } else {
      return ResponseEntity.badRequest().build();
    }
  }
}
