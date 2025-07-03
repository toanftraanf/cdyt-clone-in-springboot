package com.cdyt.be.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "role")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Role {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;
  @Column(nullable = false, length = 50)
  private String roleName;
  @Column(length = 255)
  private String description;
  @Column(nullable = false)
  private int roleType;
  private boolean isDeleted = false;
  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;
  @LastModifiedDate
  private LocalDateTime updatedAt;
  private int createdBy;
  private int updatedBy;

  @ManyToMany(mappedBy = "role")
  @JsonIgnore
  private Set<User> users;

}
