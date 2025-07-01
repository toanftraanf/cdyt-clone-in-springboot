package com.cdyt.be.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "department")
@EntityListeners(AuditingEntityListener.class)
public class Department {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  private int parentId;

  private String departmentName;

  private Boolean isDeleted;

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

  public Department(int id, int parentId, String departmentName, Boolean isDeleted,
      String departmentIdExt, String departmentPath, String departmentCode, Integer level) {
    this.id = id;
    this.parentId = parentId;
    this.departmentName = departmentName;
    this.isDeleted = isDeleted;
    this.departmentIdExt = departmentIdExt;
    this.departmentPath = departmentPath;
    this.departmentCode = departmentCode;
    this.level = level;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getParentId() {
    return parentId;
  }

  public void setParentId(int parentId) {
    this.parentId = parentId;
  }

  public String getDepartmentName() {
    return departmentName;
  }

  public void setDepartmentName(String departmentName) {
    this.departmentName = departmentName;
  }

  public Boolean getDeleted() {
    return isDeleted;
  }

  public void setDeleted(Boolean deleted) {
    isDeleted = deleted;
  }

  public String getDepartmentIdExt() {
    return departmentIdExt;
  }

  public void setDepartmentIdExt(String departmentIdExt) {
    this.departmentIdExt = departmentIdExt;
  }

  public String getDepartmentPath() {
    return departmentPath;
  }

  public void setDepartmentPath(String departmentPath) {
    this.departmentPath = departmentPath;
  }

  public String getDepartmentCode() {
    return departmentCode;
  }

  public void setDepartmentCode(String departmentCode) {
    this.departmentCode = departmentCode;
  }

  public Integer getLevel() {
    return level;
  }

  public void setLevel(Integer level) {
    this.level = level;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
