package com.cdyt.be.dto.user;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class UserResponseDto {

  private Long id;
  private String fullName;
  private String email;
  private String phone;
  private String address;
  private Integer sex;
  private LocalDate dob;
  private String avatar;
  private Boolean isActive;
  private Boolean isVerified;
  private Boolean isDeleted;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Set<RoleDto> roles;

  @Data
  public static class RoleDto {

    private int id;
    private String roleName;
    private String description;
    private int roleType;
  }
}
