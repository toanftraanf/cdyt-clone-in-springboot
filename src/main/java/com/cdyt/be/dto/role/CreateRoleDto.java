package com.cdyt.be.dto.role;

import java.util.List;

public class CreateRoleDto {

  private String roleName;
  private String description;
  private int roleType;

  public List<Long> getUserIds() {
    return userIds;
  }

  public void setUserIds(List<Long> userIds) {
    this.userIds = userIds;
  }

  public int getRoleType() {
    return roleType;
  }

  public void setRoleType(int roleType) {
    this.roleType = roleType;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getRoleName() {
    return roleName;
  }

  public void setRoleName(String roleName) {
    this.roleName = roleName;
  }

  private List<Long> userIds;
}
