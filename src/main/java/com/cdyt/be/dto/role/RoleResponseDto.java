package com.cdyt.be.dto.role;

import java.time.LocalDateTime;
import java.util.Set;
import lombok.Data;

@Data
public class RoleResponseDto {

  private int id;
  private String roleName;
  private String description;
  private int roleType;
  private boolean isDeleted = false;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private int createdBy;
  private int updatedBy;
  private Set<UserDto> users;

  @Data
  public static class UserDto {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String address;
  }
}