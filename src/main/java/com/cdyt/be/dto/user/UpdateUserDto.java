package com.cdyt.be.dto.user;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

@Data
public class UpdateUserDto {

  @Size(max = 255, message = "Full name must not exceed 255 characters")
  private String fullName;

  @Email(message = "Email should be valid")
  private String email;

  @Size(min = 6, message = "Password must be at least 6 characters")
  private String password;

  @Size(max = 20, message = "Phone must not exceed 20 characters")
  private String phone;

  private String address;

  private Integer sex;

  private LocalDate dob;

  private String avatar;

  private Boolean isActive;

  private Boolean isVerified;

  private Set<Long> roleIds;
}
