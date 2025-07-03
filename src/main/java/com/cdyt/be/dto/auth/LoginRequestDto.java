package com.cdyt.be.dto.auth;

import lombok.Data;

@Data
public class LoginRequestDto {

  private String email;
  private String password;
  private boolean isRememberPassword;
}
