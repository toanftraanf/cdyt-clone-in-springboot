package com.cdyt.be.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "usertoken")
@Data
@EntityListeners(AuditingEntityListener.class)
public class UserToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  private boolean isRememberPassword;

  @Column(length = 255, nullable = false)
  private String token;

  private LocalDateTime expiredDate;

  @CreatedDate
  private LocalDateTime createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;
}
