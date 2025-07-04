package com.cdyt.be.dto.article;

import com.cdyt.be.dto.BasePaginationRequestDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleSearchRequestDto extends BasePaginationRequestDto {

    private String title; // Optional: Search by article title (null = no title filter)

    private String status; // Optional: Filter by status (null = get ALL articles, "0" = DRAFT, "1" =
                           // PUBLISHED, "2" = ARCHIVED)

    private Long authorId; // Optional: Filter by author ID

    private Long categoryId; // Optional: Filter by category ID

    private Integer minViewCount; // Optional: Minimum view count

    private Integer maxViewCount; // Optional: Maximum view count

    // Helper methods
    public boolean hasTitleFilter() {
        return title != null && !title.trim().isEmpty();
    }

    public boolean hasStatusFilter() {
        return status != null && !status.trim().isEmpty();
    }

    public boolean hasAuthorFilter() {
        return authorId != null;
    }

    public boolean hasCategoryFilter() {
        return categoryId != null;
    }

    public boolean hasViewCountFilter() {
        return minViewCount != null || maxViewCount != null;
    }

    public boolean hasFilters() {
        return hasTitleFilter() || hasStatusFilter() || hasAuthorFilter() ||
                hasCategoryFilter() || hasViewCountFilter();
    }

    public String getCleanTitle() {
        return hasTitleFilter() ? title.trim() : null;
    }

    public String getCleanStatus() {
        return hasStatusFilter() ? status.trim().toUpperCase() : null;
    }
}