package com.cdyt.be.mapper;

import com.cdyt.be.dto.role.RoleResponseDto;
import com.cdyt.be.entity.Role;
import com.cdyt.be.entity.User;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface RoleMapper {

  @Mapping(source = "users", target = "users", qualifiedByName = "mapUsers")
  RoleResponseDto entityToResponseDto(Role role);

  @Named("mapUsers")
  default Set<RoleResponseDto.UserDto> mapUsers(Set<User> users) {
    if (users == null) {
      return null;
    }
    Set<RoleResponseDto.UserDto> usersDtos = new HashSet<>();
    for (User user : users) {
      RoleResponseDto.UserDto userDto = new RoleResponseDto.UserDto();
      userDto.setId(user.getId());
      userDto.setFullName(user.getFullName());
      userDto.setEmail(user.getEmail());
      userDto.setPhone(user.getPhone());
      userDto.setAddress(user.getAddress());
      usersDtos.add(userDto);
    }
    return usersDtos;
  }
}
