package com.cdyt.be.dto.auth;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
public class LoginResponseDto {

  private String token;
  private LocalDateTime expiredDate;
}
