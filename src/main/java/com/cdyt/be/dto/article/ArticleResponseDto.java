package com.cdyt.be.dto.article;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleResponseDto {

    private Long id;
    private String title;
    private String slug;
    private String summary;
    private Long authorId;
    private String authorName; // Just the name for display
    private Long categoryId;
    private String categoryName; // Just the name for display
    private List<TagInfo> tags; // Tag information for display
    private String coverImageUrl;
    private Integer status; // 0 = DRAFT, 1 = PUBLISHED, 2 = ARCHIVED
    private LocalDateTime publishedAt;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Status constants
    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_PUBLISHED = 1;
    public static final int STATUS_ARCHIVED = 2;

    // Nested class for tag information
    @Data
    public static class TagInfo {
        private Long id;
        private String name;
        private String color;
    }

    // Convenience methods for display

    public boolean isPublished() {
        return status != null && status == STATUS_PUBLISHED;
    }

    public boolean isDraft() {
        return status != null && status == STATUS_DRAFT;
    }

    public boolean isArchived() {
        return status != null && status == STATUS_ARCHIVED;
    }

    public String getStatusDisplay() {
        if (status == null)
            return "unknown";
        return switch (status) {
            case STATUS_DRAFT -> "draft";
            case STATUS_PUBLISHED -> "published";
            case STATUS_ARCHIVED -> "archived";
            default -> "unknown";
        };
    }

    public String getStatusDisplayCapitalized() {
        if (status == null)
            return "Unknown";
        return switch (status) {
            case STATUS_DRAFT -> "Draft";
            case STATUS_PUBLISHED -> "Published";
            case STATUS_ARCHIVED -> "Archived";
            default -> "Unknown";
        };
    }

    public boolean hasCoverImage() {
        return coverImageUrl != null && !coverImageUrl.trim().isEmpty();
    }

    public boolean hasSummary() {
        return summary != null && !summary.trim().isEmpty();
    }
}