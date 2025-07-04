package com.cdyt.be.dto.article;

import com.cdyt.be.dto.BasePaginationRequestDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PublicArticleSearchRequestDto extends BasePaginationRequestDto {

    private String title;
    private String fromDate; // YYYY-MM-DD format
    private String toDate; // YYYY-MM-DD format

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

    // Validation helpers
    public boolean isValidPage() {
        return getPage() >= 0;
    }

    public boolean isValidSize() {
        return getSize() > 0 && getSize() <= 100;
    }
}