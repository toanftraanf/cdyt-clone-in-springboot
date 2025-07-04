package com.cdyt.be.dto.category;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CategoryResponseDto {

  private Long id;
  private String name;
  private String slug;
  private String description;
  private Integer displayOrder;
  private Integer status;

  // Parent category information
  private Long parentId;
  private String parentName;

  // Children categories (for hierarchical display)
  private List<CategoryResponseDto> children;

  // Additional computed fields
  private String fullPath;
  private Integer depthLevel;
  private Boolean hasChildren;
  private Boolean isRootCategory;
}