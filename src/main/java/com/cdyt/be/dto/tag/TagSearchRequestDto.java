package com.cdyt.be.dto.tag;

import com.cdyt.be.dto.BasePaginationRequestDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TagSearchRequestDto extends BasePaginationRequestDto {

    private String name; // Optional: Search by tag name (null = no name filter)

    private Integer status; // Optional: Filter by status (null = get ALL tags, 1=active only, 0=inactive
                            // only)

    private String color; // Optional: Filter by color (hex code)

    private Long minUsageCount; // Optional: Minimum usage count

    private Long maxUsageCount; // Optional: Maximum usage count

    // Helper methods
    public boolean hasNameFilter() {
        return name != null && !name.trim().isEmpty();
    }

    public boolean hasStatusFilter() {
        return status != null;
    }

    public boolean hasUsageCountFilter() {
        return minUsageCount != null || maxUsageCount != null;
    }

    public boolean hasColorFilter() {
        return color != null && !color.trim().isEmpty();
    }

    public boolean hasFilters() {
        return hasNameFilter() || hasStatusFilter() || hasUsageCountFilter() || hasColorFilter();
    }

    public String getCleanName() {
        return hasNameFilter() ? name.trim() : null;
    }

    public String getCleanColor() {
        return hasColorFilter() ? color.trim() : null;
    }
}