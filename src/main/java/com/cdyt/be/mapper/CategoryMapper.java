package com.cdyt.be.mapper;

import com.cdyt.be.dto.category.CategoryResponseDto;
import com.cdyt.be.dto.category.CreateCategoryDto;
import com.cdyt.be.dto.category.UpdateCategoryDto;
import com.cdyt.be.entity.Category;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {

  /**
   * Convert Category entity to CategoryResponseDto
   */
  public CategoryResponseDto toResponseDto(Category category) {
    if (category == null) {
      return null;
    }

    CategoryResponseDto dto = new CategoryResponseDto();
    dto.setId(category.getId());
    dto.setName(category.getName());
    dto.setSlug(category.getSlug());
    dto.setDescription(category.getDescription());
    dto.setDisplayOrder(category.getDisplayOrder());
    dto.setStatus(category.getStatus());
   
    // Parent information
    if (category.getParent() != null) {
      dto.setParentId(category.getParent().getId());
      dto.setParentName(category.getParent().getName());
    }

    // Computed fields
    dto.setFullPath(category.getFullPath());
    dto.setDepthLevel(category.getDepthLevel());
    dto.setHasChildren(category.hasChildren());
    dto.setIsRootCategory(category.isRootCategory());

    return dto;
  }

  /**
   * Convert Category entity to CategoryResponseDto with children
   */
  public CategoryResponseDto toResponseDtoWithChildren(Category category) {
    CategoryResponseDto dto = toResponseDto(category);

    if (category.getChildren() != null && !category.getChildren().isEmpty()) {
      List<CategoryResponseDto> childrenDtos = category.getChildren().stream()
          .filter(child -> !child.getIsDeleted())
          .map(this::toResponseDtoWithChildren) // Recursive for nested children
          .collect(Collectors.toList());
      dto.setChildren(childrenDtos);
    }

    return dto;
  }

  /**
   * Convert Category entity to CategoryResponseDto without children (for performance)
   */
  public CategoryResponseDto toResponseDtoWithoutChildren(Category category) {
    return toResponseDto(category);
  }

  /**
   * Convert CreateCategoryDto to Category entity
   */
  public Category toEntity(CreateCategoryDto createDto) {
    if (createDto == null) {
      return null;
    }

    Category category = new Category();
    category.setName(createDto.getName());
    category.setDescription(createDto.getDescription());
    category.setDisplayOrder(createDto.getDisplayOrder());
    category.setStatus(createDto.getStatus());
    category.setIsDeleted(false);

    return category;
  }

  /**
   * Update Category entity with UpdateCategoryDto data
   */
  public void updateEntityFromDto(Category category, UpdateCategoryDto updateDto) {
    if (updateDto == null || category == null) {
      return;
    }

    if (updateDto.getName() != null) {
      category.setName(updateDto.getName());
    }

    if (updateDto.getSlug() != null) {
      category.setSlug(updateDto.getSlug());
    }

    if (updateDto.getDescription() != null) {
      category.setDescription(updateDto.getDescription());
    }

    if (updateDto.getDisplayOrder() != null) {
      category.setDisplayOrder(updateDto.getDisplayOrder());
    }

    if (updateDto.getStatus() != null) {
      category.setStatus(updateDto.getStatus());
    }
  }

  /**
   * Convert list of Category entities to list of CategoryResponseDto
   */
  public List<CategoryResponseDto> toResponseDtoList(List<Category> categories) {
    return categories.stream()
        .map(this::toResponseDto)
        .collect(Collectors.toList());
  }

  /**
   * Convert list of Category entities to list of CategoryResponseDto with children
   */
  public List<CategoryResponseDto> toResponseDtoListWithChildren(List<Category> categories) {
    return categories.stream()
        .map(this::toResponseDtoWithChildren)
        .collect(Collectors.toList());
  }
}