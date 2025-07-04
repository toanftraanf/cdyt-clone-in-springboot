package com.cdyt.be.dto.tag;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TagSearchResultDto {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String color;
    private Integer status;
    private Long usageCount;
    private Integer displayOrder;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isPopular;
    private Long totalCount; // For pagination

    // Constructor for mapping from Map<String, Object>
    public TagSearchResultDto(java.util.Map<String, Object> row) {
        this.id = ((Number) row.get("id")).longValue();
        this.name = (String) row.get("name");
        this.slug = (String) row.get("slug");
        this.description = (String) row.get("description");
        this.color = (String) row.get("color");
        this.status = row.get("status") != null ? ((Number) row.get("status")).intValue() : null;
        this.usageCount = row.get("usage_count") != null ? ((Number) row.get("usage_count")).longValue() : 0L;
        this.displayOrder = row.get("display_order") != null ? ((Number) row.get("display_order")).intValue() : null;
        this.isDeleted = (Boolean) row.get("is_deleted");
        this.createdAt = row.get("created_at") != null ? (LocalDateTime) row.get("created_at") : null;
        this.updatedAt = row.get("updated_at") != null ? (LocalDateTime) row.get("updated_at") : null;
        this.isPopular = (Boolean) row.get("is_popular");
        this.totalCount = row.get("total_count") != null ? ((Number) row.get("total_count")).longValue() : 0L;
    }
}