package com.cdyt.be.dto.article;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleDetailResponseDto {

    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String content;
    private AuthorInfo author; // Full author information
    private CategoryInfo category; // Full category information
    private List<TagInfo> tags; // Detailed tag information
    private String coverImageUrl;
    private Integer status; // 0 = DRAFT, 1 = PUBLISHED, 2 = ARCHIVED
    private LocalDateTime publishedAt;
    private Integer viewCount;
    private Integer likeCount;
    private Integer commentCount;
    private String seoTitle;
    private String seoDescription;
    private String seoKeywords;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Status constants
    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_PUBLISHED = 1;
    public static final int STATUS_ARCHIVED = 2;

    // Nested classes for related entities

    @Data
    public static class AuthorInfo {
        private Long id;
        private String name;
        private String avatar;
        private String email;
    }

    @Data
    public static class CategoryInfo {
        private Long id;
        private String name;
        private String description;
    }

    @Data
    public static class TagInfo {
        private Long id;
        private String name;
        private String slug;
        private String description;
        private String color;
        private Long usageCount;
    }

    // Convenience methods for display

    public boolean isPublished() {
        return status != null && status == STATUS_PUBLISHED && !isDeleted;
    }

    public boolean isDraft() {
        return status != null && status == STATUS_DRAFT && !isDeleted;
    }

    public boolean isArchived() {
        return status != null && status == STATUS_ARCHIVED && !isDeleted;
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

    public boolean hasContent() {
        return content != null && !content.trim().isEmpty();
    }

    public boolean hasSummary() {
        return summary != null && !summary.trim().isEmpty();
    }
}