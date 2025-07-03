package com.cdyt.be.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "department")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Department {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  private int parentId;

  private String departmentName;

  private boolean isDeleted = false;

  private String departmentIdExt;

  private String departmentPath;

  private String departmentCode;

  private Integer level;

  @CreatedBy
  @Column(updatable = false)
  private String createdBy;

  @CreatedDate
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedBy
  private String updatedBy;

  @LastModifiedDate
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime updatedAt;

  // Default constructor for JPA
  public Department() {
  }
}
