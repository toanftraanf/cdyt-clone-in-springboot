package com.cdyt.be.dto.category;

import com.cdyt.be.dto.SearchRequestDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CategorySearchDto extends SearchRequestDto {

    // Specific field searches
    private String name; // Exact or partial name match
    private String slug; // Exact or partial slug match
    private String description; // Search in description

    // Category-specific filters
    private LocalDateTime createDate; // Exact creation date
    private Integer status; // 1=active, 0=inactive
    private Long parentId; // Filter by parent category
    private Boolean isRootCategory; // Only root categories (parentId is null)
    private Boolean isDeleted; // Include deleted categories or not
    private Boolean hasChildren; // Categories that have child categories

    // Display order filters
    private Integer minDisplayOrder;
    private Integer maxDisplayOrder;

    // Date range filters
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    private LocalDateTime updatedAfter;
    private LocalDateTime updatedBefore;

    // Advanced filters
    private List<Long> ids; // Filter by specific IDs
    private List<String> excludeSlugs; // Exclude specific slugs
    private List<Long> excludeParentIds; // Exclude categories under specific parents

    // Sorting helpers (for complex sorts)
    private String sortBy = "displayOrder"; // Field to sort by
    private String sortDirection = "asc"; // asc or desc

    // Helper methods
    public boolean hasSearch() {
        return hasTextSearch();
    }

    public boolean hasSpecificFieldSearch() {
        return (name != null && !name.trim().isEmpty()) ||
                (slug != null && !slug.trim().isEmpty()) ||
                (description != null && !description.trim().isEmpty());
    }

    public boolean hasFilters() {
        return createDate != null || status != null || parentId != null ||
                isRootCategory != null || isDeleted != null || hasChildren != null ||
                minDisplayOrder != null || maxDisplayOrder != null ||
                createdAfter != null || createdBefore != null ||
                updatedAfter != null || updatedBefore != null ||
                ids != null || excludeSlugs != null || excludeParentIds != null;
    }

    public boolean isEmpty() {
        return !hasTextSearch() && !hasSpecificFieldSearch() && !hasFilters();
    }

    public boolean hasDateRangeFilter() {
        return createdAfter != null || createdBefore != null ||
                updatedAfter != null || updatedBefore != null;
    }

    public boolean hasHierarchyFilter() {
        return parentId != null || isRootCategory != null || hasChildren != null ||
                excludeParentIds != null;
    }
}