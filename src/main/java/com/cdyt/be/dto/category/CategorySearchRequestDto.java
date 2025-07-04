package com.cdyt.be.dto.category;

import com.cdyt.be.dto.BasePaginationRequestDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CategorySearchRequestDto extends BasePaginationRequestDto {

    private String name; // Optional: Search by category name (null = no name filter)

    private Integer status; // Optional: Filter by status (null = get ALL categories, 1=active only,
                            // 0=inactive only)

    // Helper methods
    public boolean hasNameFilter() {
        return name != null && !name.trim().isEmpty();
    }

    public boolean hasStatusFilter() {
        return status != null;
    }

    public boolean hasFilters() {
        return hasNameFilter() || hasStatusFilter();
    }

    public String getCleanName() {
        return hasNameFilter() ? name.trim() : null;
    }
}