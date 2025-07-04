package com.cdyt.be.controller;

import com.cdyt.be.common.annotation.RequireAuth;
import com.cdyt.be.common.controller.BaseAuthController;
import com.cdyt.be.common.dto.ApiResponse;
import com.cdyt.be.common.util.ResponseUtils;
import com.cdyt.be.dto.category.CategoryResponseDto;
import com.cdyt.be.dto.category.CategorySearchRequestDto;
import com.cdyt.be.dto.category.CreateCategoryDto;
import com.cdyt.be.dto.category.UpdateCategoryDto;
import com.cdyt.be.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@RequireAuth
@Tag(name = "Category Management", description = "APIs for managing categories")
public class CategoryController extends BaseAuthController {

  private final CategoryService categoryService;

  @PostMapping("/getAll")
  @Operation(summary = "Search categories with pagination and filters", description = "Search categories using POST with pagination and optional filters in request body")
  public ResponseEntity<ApiResponse<Page<CategoryResponseDto>>> searchCategories(
      @Valid @RequestBody CategorySearchRequestDto searchRequest) {
    Page<CategoryResponseDto> categories = categoryService.searchCategories(searchRequest);
    return ok(categories, "Categories search completed successfully",
        Map.of("totalElements", categories.getTotalElements(),
            "totalPages", categories.getTotalPages(),
            "currentPage", categories.getNumber(),
            "pageSize", categories.getSize(),
            "hasFilters", searchRequest.hasFilters()));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get category by ID", description = "Retrieves a category by its unique identifier")
  public ResponseEntity<ApiResponse<CategoryResponseDto>> getCategoryById(
      @Parameter(description = "Category ID") @PathVariable Long id) {
    return categoryService.getCategoryById(id)
        .map(category -> ok(category, "Category found successfully",
            ResponseUtils.operationMetadata("getCategoryById", id)))
        .orElse(notFound("Category not found with ID: " + id));
  }

  @GetMapping("/slug/{slug}")
  @Operation(summary = "Get category by slug", description = "Retrieves a category by its slug")
  public ResponseEntity<ApiResponse<CategoryResponseDto>> getCategoryBySlug(
      @Parameter(description = "Category slug") @PathVariable String slug) {
    return categoryService.getCategoryBySlug(slug)
        .map(category -> ok(category, "Category found successfully",
            ResponseUtils.operationMetadata("getCategoryBySlug", slug)))
        .orElse(notFound("Category not found with slug: " + slug));
  }

  @PostMapping
  @Operation(summary = "Create a new category", description = "Creates a new category with the provided information")
  public ResponseEntity<ApiResponse<CategoryResponseDto>> createCategory(
      @Valid @RequestBody CreateCategoryDto createDto) {
    CategoryResponseDto createdCategory = categoryService.createCategory(createDto);
    return created(createdCategory, "Category created successfully",
        ResponseUtils.operationMetadata("createCategory", createdCategory.getSlug()));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update category", description = "Updates an existing category's information")
  public ResponseEntity<ApiResponse<CategoryResponseDto>> updateCategory(
      @Parameter(description = "Category ID") @PathVariable Long id,
      @Valid @RequestBody UpdateCategoryDto updateDto) {
    try {
      CategoryResponseDto updatedCategory = categoryService.updateCategory(id, updateDto);
      return ok(updatedCategory, "Category updated successfully",
          ResponseUtils.operationMetadata("updateCategory", id));
    } catch (RuntimeException e) {
      return notFound("Category not found with ID: " + id);
    }
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete category", description = "Soft deletes a category (marks as deleted)")
  public ResponseEntity<ApiResponse<Void>> deleteCategory(
      @Parameter(description = "Category ID") @PathVariable Long id) {
    try {
      categoryService.deleteCategory(id);
      return ok(null, "Category deleted successfully",
          ResponseUtils.operationMetadata("deleteCategory", id));
    } catch (RuntimeException e) {
      return notFound("Category not found with ID: " + id);
    }
  }
}