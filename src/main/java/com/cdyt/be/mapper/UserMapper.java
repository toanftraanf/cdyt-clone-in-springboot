package com.cdyt.be.mapper;

import com.cdyt.be.dto.user.CreateUserDto;
import com.cdyt.be.dto.user.UpdateUserDto;
import com.cdyt.be.dto.user.UserResponseDto;
import com.cdyt.be.entity.Role;
import com.cdyt.be.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "password", ignore = true) // Password will be set separately after encoding
  @Mapping(target = "fullNameNoMark", ignore = true) // Handled by custom setter in entity
  @Mapping(target = "role", ignore = true) // Will be set separately from roleIds
  @Mapping(target = "isActive", constant = "false")
  @Mapping(target = "isVerified", constant = "false")
  @Mapping(target = "isDeleted", constant = "false")
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "expiredAt", ignore = true)
  @Mapping(target = "verifyCode", ignore = true)
  User createDtoToEntity(CreateUserDto createUserDto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "password", ignore = true) // Password handled separately if provided
  @Mapping(target = "fullNameNoMark", ignore = true) // Handled by custom setter in entity
  @Mapping(target = "role", ignore = true) // Will be set separately from roleIds
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "expiredAt", ignore = true)
  @Mapping(target = "verifyCode", ignore = true)
  @Mapping(target = "isDeleted", ignore = true)
  // Don't allow updating delete status through update DTO
  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntityFromDto(UpdateUserDto updateUserDto, @MappingTarget User user);

  @Mapping(source = "role", target = "roles", qualifiedByName = "mapRoles")
  UserResponseDto entityToResponseDto(User user);

  @Named("mapRoles")
  default java.util.Set<UserResponseDto.RoleDto> mapRoles(java.util.Set<Role> roles) {
    if (roles == null) {
      return null;
    }
    java.util.Set<UserResponseDto.RoleDto> roleDtos = new java.util.HashSet<>();
    for (Role role : roles) {
      UserResponseDto.RoleDto roleDto = new UserResponseDto.RoleDto();
      roleDto.setId(role.getId());
      roleDto.setRoleName(role.getRoleName());
      roleDto.setDescription(role.getDescription());
      roleDto.setRoleType(role.getRoleType());
      roleDtos.add(roleDto);
    }
    return roleDtos;
  }
}
