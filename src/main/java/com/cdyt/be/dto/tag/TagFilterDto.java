package com.cdyt.be.dto.tag;

import com.cdyt.be.dto.SearchRequestDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class TagFilterDto extends SearchRequestDto {

    // Specific field searches
    private String name; // Exact or partial name match
    private String slug; // Exact or partial slug match
    private String description; // Search in description

    // Filter parameters
    private Integer status; // Filter by status (1=active, 0=inactive)
    private String color; // Filter by color
    private List<String> colors; // Filter by multiple colors
    private Boolean isDeleted; // Include deleted tags or not
    private Boolean isActive; // Filter by active status

    // Usage count filters
    private Long minUsageCount; // Minimum usage count
    private Long maxUsageCount; // Maximum usage count
    private Boolean isPopular; // Filter popular tags (usage >= 10)
    private Boolean isUnused; // Filter unused tags (usage = 0)

    // Display order filters
    private Integer minDisplayOrder;
    private Integer maxDisplayOrder;

    // Date range filters
    private LocalDateTime createdAfter; // Created after this date
    private LocalDateTime createdBefore; // Created before this date
    private LocalDateTime updatedAfter; // Updated after this date
    private LocalDateTime updatedBefore; // Updated before this date

    // Advanced filters
    private Boolean hasColor; // Tags with or without custom colors
    private List<Long> ids; // Filter by specific IDs
    private List<String> excludeSlugs; // Exclude specific slugs

    // Sorting helpers (for complex sorts)
    private String sortBy; // Field to sort by
    private String sortDirection; // asc or desc

    // Helper methods
    public boolean hasSearch() {
        return hasTextSearch();
    }

    public boolean hasFilters() {
        return status != null || color != null || colors != null ||
                isDeleted != null || isActive != null ||
                minUsageCount != null || maxUsageCount != null ||
                isPopular != null || isUnused != null ||
                minDisplayOrder != null || maxDisplayOrder != null ||
                createdAfter != null || createdBefore != null ||
                updatedAfter != null || updatedBefore != null ||
                hasColor != null || ids != null || excludeSlugs != null;
    }

    public boolean isEmpty() {
        return !hasSearch() && !hasFilters() &&
                (name == null || name.trim().isEmpty()) &&
                (slug == null || slug.trim().isEmpty()) &&
                (description == null || description.trim().isEmpty());
    }
}