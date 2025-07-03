package com.cdyt.be.service;

import com.cdyt.be.dto.role.CreateRoleDto;
import com.cdyt.be.dto.role.RoleResponseDto;
import com.cdyt.be.entity.Role;
import com.cdyt.be.entity.User;
import com.cdyt.be.mapper.RoleMapper;
import com.cdyt.be.repository.RoleRepository;
import com.cdyt.be.repository.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService {

  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final RoleMapper roleMapper;

  public List<RoleResponseDto> getAllRoles() {
    return roleRepository.findAll().stream().map(roleMapper::entityToResponseDto).toList();
  }

  public Optional<RoleResponseDto> getRoleById(int id) {
    return roleRepository.findById(id).map(roleMapper::entityToResponseDto);
  }

  public RoleResponseDto createRole(CreateRoleDto input) {
    Role newRole = new Role();
    newRole.setRoleName(input.getRoleName());
    newRole.setDescription(input.getDescription());
    newRole.setRoleType(input.getRoleType());

    List<Long> usersList = input.getUserIds();
    if (usersList != null && !usersList.isEmpty()) {
      Set<User> users = new HashSet<>(userRepository.findAllById(usersList));
      newRole.setUsers(users);
      for (User user : users) {
        user.getRole().add(newRole);
      }
    }

    Role savedRole = roleRepository.save(newRole);
    return roleMapper.entityToResponseDto(savedRole);
  }

  public Role updateRole(int id, Role updatedRole) {
    return null;
  }

  public void deleteRole(int id) {

  }
}
