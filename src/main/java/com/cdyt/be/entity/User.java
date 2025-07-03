package com.cdyt.be.entity;

import com.cdyt.be.util.TextUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String fullName;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  @JsonIgnore
  @ToString.Exclude
  private String password;

  @Column(unique = true)
  private String phone;

  private String address;

  private String fullNameNoMark;

  private Integer sex = 0;

  private LocalDate dob;

  private String verifyCode;

  private String avatar = "https://avatar.iran.liara.run/public/9";

  private LocalDateTime expiredAt;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  private Boolean isActive = false;

  private Boolean isVerified = false;

  private Boolean isDeleted = false;

  @ManyToMany
  @JoinTable(
      name = "user_role",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id")
  )
  private Set<Role> role = new HashSet<>();

  // Custom setter for fullName to maintain the fullNameNoMark logic
  public void setFullName(String fullName) {
    this.fullName = fullName;
    this.fullNameNoMark = (fullName == null) ? null : TextUtils.removeAccents(fullName);
  }
}
