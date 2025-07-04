package com.cdyt.be.dto.article;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArticlesByTagSearchRequestDto {

    @NotNull(message = "Tag ID is required")
    private Long tagId;

    @Min(value = 0, message = "Page number must be >= 0")
    private int page = 0;

    @Min(value = 1, message = "Page size must be >= 1")
    @Max(value = 100, message = "Page size must be <= 100")
    private int size = 10;

    private String title; // Search in article title
    private String fromDate; // YYYY-MM-DD format - published date range start
    private String toDate; // YYYY-MM-DD format - published date range end

    // Helper methods

    public String getCleanTitle() {
        return (title != null && !title.trim().isEmpty()) ? title.trim() : null;
    }

    public boolean hasFilters() {
        return getCleanTitle() != null || fromDate != null || toDate != null;
    }

    public boolean hasDateRange() {
        return fromDate != null || toDate != null;
    }

    public boolean hasTitleSearch() {
        return getCleanTitle() != null;
    }

    // Validation helpers
    public boolean isValidPage() {
        return page >= 0;
    }

    public boolean isValidSize() {
        return size > 0 && size <= 100; // Max 100 items per page
    }
}