package com.cdyt.be.service;

import com.cdyt.be.dto.role.CreateRoleDto;
import com.cdyt.be.entity.Role;
import com.cdyt.be.entity.User;
import com.cdyt.be.repository.RoleRepository;
import com.cdyt.be.repository.UserRepositoty;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class RoleService {

  private final RoleRepository roleRepository;
  private final UserRepositoty userRepository;

  public RoleService(RoleRepository roleRepository, UserRepositoty userRepository) {
    this.roleRepository = roleRepository;
    this.userRepository = userRepository;
  }

  public List<Role> getAllRoles() {
    return roleRepository.findAll();
  }


  public Optional<Role> getRoleById(int id) {
    return roleRepository.findById(id);
  }

  public Role createRole(CreateRoleDto input) {
    Role newRole = new Role();
    newRole.setRoleName(input.getRoleName());
    newRole.setDescription(input.getDescription());
    newRole.setRoleType(input.getRoleType());

    List<Long> usersList = input.getUserIds();
    if (usersList != null && usersList.isEmpty()) {
      Set<User> users = new HashSet<>(userRepository.findAllById(usersList));
      newRole.setUsers(users);
      for (User user : users) {
        user.getRole().add(new Role());
      }
    }
    return roleRepository.save(newRole);
  }

  public Role updateRole(int id, Role updatedRole) {
    return null;
  }

  public void deleteRole(int id) {

  }
}
