package com.cdyt.be.service;

import com.cdyt.be.common.exception.BusinessException;
import com.cdyt.be.dto.category.CategoryResponseDto;
import com.cdyt.be.dto.category.CategorySearchRequestDto;
import com.cdyt.be.dto.category.CreateCategoryDto;
import com.cdyt.be.dto.category.UpdateCategoryDto;
import com.cdyt.be.entity.Category;
import com.cdyt.be.mapper.CategoryMapper;
import com.cdyt.be.repository.CategoryRepository;
import com.cdyt.be.util.TextUtils;
import com.cdyt.be.util.CacheNames;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    /**
     * Search categories with pagination and optional filters using request DTO
     * 
     * Filter behavior:
     * - name = null: Returns all categories (no name filtering)
     * - status = null: Returns ALL categories (both active and inactive)
     * - status = 1: Returns only active categories
     * - status = 0: Returns only inactive categories
     */
    public Page<CategoryResponseDto> searchCategories(CategorySearchRequestDto searchRequest) {
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());
        Page<Category> categories = categoryRepository.findCategoriesWithFilters(
                searchRequest.getCleanName(),
                searchRequest.getStatus(),
                pageable);

        List<CategoryResponseDto> categoryDtos = categoryMapper.toResponseDtoList(categories.getContent());
        return new PageImpl<>(categoryDtos, pageable, categories.getTotalElements());
    }

    /**
     * Get all active categories
     */
    public List<CategoryResponseDto> getAllActiveCategories() {
        List<Category> categories = categoryRepository.findByStatusAndIsDeletedFalseOrderByDisplayOrderAscNameAsc(1);
        return categoryMapper.toResponseDtoList(categories);
    }

    /**
     * Get category hierarchy (root categories with their children recursively)
     */
    @org.springframework.cache.annotation.Cacheable(CacheNames.CATEGORY_HIERARCHY)
    public List<CategoryResponseDto> getCategoryHierarchy() {
        List<Category> rootCategories = categoryRepository
                .findByParentIsNullAndIsDeletedFalseOrderByDisplayOrderAscNameAsc();
        return categoryMapper.toResponseDtoListWithChildren(rootCategories);
    }

    /**
     * Get root categories only
     */
    public List<CategoryResponseDto> getRootCategories() {
        List<Category> rootCategories = categoryRepository
                .findByParentIsNullAndIsDeletedFalseOrderByDisplayOrderAscNameAsc();
        return categoryMapper.toResponseDtoList(rootCategories);
    }

    /**
     * Get children of a specific category
     */
    public List<CategoryResponseDto> getCategoryChildren(Long parentId) {
        List<Category> children = categoryRepository
                .findByParentIdAndIsDeletedFalseOrderByDisplayOrderAscNameAsc(parentId);
        return categoryMapper.toResponseDtoList(children);
    }

    /**
     * Get category by ID
     */
    public Optional<CategoryResponseDto> getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .filter(category -> !category.getIsDeleted())
                .map(categoryMapper::toResponseDto);
    }

    /**
     * Get category by slug
     */
    public Optional<CategoryResponseDto> getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .filter(category -> !category.getIsDeleted())
                .map(categoryMapper::toResponseDto);
    }

    /**
     * Search categories by name (simple search)
     */
    public List<CategoryResponseDto> searchCategoriesByName(String name) {
        List<Category> categories = categoryRepository
                .findByNameContainingIgnoreCaseAndIsDeletedFalseOrderByNameAsc(name);
        return categoryMapper.toResponseDtoList(categories);
    }

    /**
     * Create a new category
     */
    @org.springframework.cache.annotation.CacheEvict(value = CacheNames.CATEGORY_HIERARCHY, allEntries = true)
    public CategoryResponseDto createCategory(CreateCategoryDto createDto) {
        Category category = categoryMapper.toEntity(createDto);

        // Generate unique slug from name
        String baseSlug = TextUtils.generateSlug(createDto.getName());
        String uniqueSlug = generateUniqueSlug(baseSlug);
        category.setSlug(uniqueSlug);

        // Set parent if provided
        if (createDto.getParentId() != null) {
            Category parent = categoryRepository.findById(createDto.getParentId())
                    .orElseThrow(() -> BusinessException.notFound("Parent category", createDto.getParentId()));
            category.setParent(parent);
        }

        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toResponseDto(savedCategory);
    }

    /**
     * Update an existing category
     */
    @org.springframework.cache.annotation.CacheEvict(value = CacheNames.CATEGORY_HIERARCHY, allEntries = true)
    public CategoryResponseDto updateCategory(Long id, UpdateCategoryDto updateDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Category", id));

        if (category.getIsDeleted()) {
            throw BusinessException.invalidState("Cannot update deleted category");
        }

        // Handle slug generation/validation
        if (updateDto.getSlug() != null) {
            // User provided custom slug - validate uniqueness
            if (!updateDto.getSlug().equals(category.getSlug()) &&
                    categoryRepository.existsBySlugAndIdNot(updateDto.getSlug(), id)) {
                throw BusinessException.alreadyExists("Category with slug", updateDto.getSlug());
            }
        } else if (updateDto.getName() != null && !updateDto.getName().equals(category.getName())) {
            // Name changed but no custom slug provided - generate new slug
            String baseSlug = TextUtils.generateSlug(updateDto.getName());
            String uniqueSlug = generateUniqueSlug(baseSlug, id);
            updateDto.setSlug(uniqueSlug);
        }

        // Update parent if provided
        if (updateDto.getParentId() != null) {
            if (updateDto.getParentId().equals(id)) {
                throw BusinessException.invalidInput("Category cannot be its own parent");
            }

            Category newParent = categoryRepository.findById(updateDto.getParentId())
                    .orElseThrow(() -> BusinessException.notFound("Parent category", updateDto.getParentId()));

            if (newParent.getIsDeleted()) {
                throw BusinessException.invalidState("Cannot set deleted category as parent");
            }

            // Prevent circular references
            if (isDescendantOf(newParent, category)) {
                throw BusinessException.invalidState("Cannot set a descendant category as parent (circular reference)");
            }

            category.setParent(newParent);
        } else if (updateDto.getParentId() == null) {
            // Explicitly setting parent to null (making it a root category)
            category.setParent(null);
        }

        categoryMapper.updateEntityFromDto(category, updateDto);
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toResponseDto(savedCategory);
    }

    /**
     * Delete a category
     */
    @org.springframework.cache.annotation.CacheEvict(value = CacheNames.CATEGORY_HIERARCHY, allEntries = true)
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Category", id));

        if (category.getIsDeleted()) {
            throw BusinessException.invalidState("Category is already deleted");
        }

        // Check if category has children
        if (category.hasChildren()) {
            // Option 1: Prevent deletion if has children
            throw BusinessException
                    .invalidState("Cannot delete category with children. Please delete or move children first.");

            // Option 2: Alternative - recursively delete children (uncomment if needed)
            // deleteCategoryAndChildren(category);
        }

        category.setIsDeleted(true);
        categoryRepository.save(category);
    }

    /**
     * Permanently delete a category (hard delete)
     */
    @org.springframework.cache.annotation.CacheEvict(value = CacheNames.CATEGORY_HIERARCHY, allEntries = true)
    public void permanentlyDeleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Category", id));

        if (category.hasChildren()) {
            throw BusinessException.invalidState("Cannot permanently delete category with children");
        }

        categoryRepository.delete(category);
    }

    // Helper methods

    /**
     * Check if a category is a descendant of another category (to prevent circular
     * references)
     */
    private boolean isDescendantOf(Category potentialAncestor, Category category) {
        Category parent = category.getParent();
        while (parent != null) {
            if (parent.getId().equals(potentialAncestor.getId())) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    /**
     * Generate a unique slug by checking database and appending numbers if needed
     */
    private String generateUniqueSlug(String baseSlug) {
        return generateUniqueSlug(baseSlug, null);
    }

    /**
     * Generate a unique slug by checking database and appending numbers if needed
     * 
     * @param baseSlug  The base slug to make unique
     * @param excludeId ID to exclude from uniqueness check (for updates)
     */
    private String generateUniqueSlug(String baseSlug, Long excludeId) {
        if (baseSlug == null || baseSlug.isEmpty()) {
            baseSlug = "category";
        }

        String slug = baseSlug;
        int counter = 1;

        // Check if slug exists (excluding the current category for updates)
        while (excludeId != null
                ? categoryRepository.existsBySlugAndIdNot(slug, excludeId)
                : categoryRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }

        return slug;
    }

    /**
     * Recursively delete category and all its children (alternative implementation)
     */
    @Transactional
    protected void deleteCategoryAndChildren(Category category) {
        // First delete all children
        for (Category child : category.getChildren()) {
            if (!child.getIsDeleted()) {
                deleteCategoryAndChildren(child);
            }
        }
        // Then delete the category itself
        category.setIsDeleted(true);
        categoryRepository.save(category);
    }

}