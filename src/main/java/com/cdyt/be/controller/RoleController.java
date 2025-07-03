package com.cdyt.be.controller;

import com.cdyt.be.common.annotation.RequireAuth;
import com.cdyt.be.common.controller.BaseAuthController;
import com.cdyt.be.common.dto.ApiResponse;
import com.cdyt.be.common.util.ResponseUtils;
import com.cdyt.be.dto.role.CreateRoleDto;
import com.cdyt.be.dto.role.RoleResponseDto;
import com.cdyt.be.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("api/roles")
@Tag(name = "Role", description = "APIs for managing roles")
@RequiredArgsConstructor
@RequireAuth
public class RoleController extends BaseAuthController {

  private final RoleService roleService;

  @Operation(summary = "Get all roles", description = "Retrieves a list of all roles")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Roles retrieved successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "No roles found")
  })
  @GetMapping("/getAll")
  public ResponseEntity<ApiResponse<List<RoleResponseDto>>> findAll() {
    List<RoleResponseDto> roles = roleService.getAllRoles();
    return ok(roles, "Roles retrieved successfully", ResponseUtils.listMetadata(roles.size()));
  }

  @Operation(summary = "Get role by ID", description = "Retrieves a role by its unique identifier")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Role found"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Role not found")
  })
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<RoleResponseDto>> findById(
      @Parameter(description = "RoleID") @PathVariable int id) {
    return roleService.getRoleById(id)
        .map(role -> ok(role, "Role found successfully", ResponseUtils.operationMetadata("getRoleById", id)))
        .orElse(notFound("Role not found with ID: " + id));
  }

  @Operation(summary = "Create a new role", description = "Creates a new role with the provided information")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Role created successfully"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data")
  })
  @PostMapping
  public ResponseEntity<ApiResponse<RoleResponseDto>> createRole(@RequestBody @Valid CreateRoleDto role) {
    RoleResponseDto savedRole = roleService.createRole(role);
    return created(savedRole, "Role created successfully",
        ResponseUtils.operationMetadata("createRole", savedRole.getRoleName()));
  }
}
