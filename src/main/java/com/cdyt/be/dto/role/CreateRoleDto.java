package com.cdyt.be.dto.role;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Data;

@Data
public class CreateRoleDto {

  @NotBlank(message = "Role name is required")
  private String roleName;
  private String description;
  private int roleType;
  private List<Long> userIds;
}
