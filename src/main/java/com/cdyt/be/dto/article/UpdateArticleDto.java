package com.cdyt.be.dto.article;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

/**
 * DTO for updating articles with partial update support
 */
@Data
public class UpdateArticleDto {

    @NotNull(message = "Article ID is required")
    private Long id;

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 1000, message = "Summary must not exceed 1000 characters")
    private String summary;

    private String content;

    private Long categoryId; // Use 0 to remove category, null to keep current

    private Set<Long> tagIds; // Tag IDs to associate with this article, empty set to remove all tags

    @Size(max = 255, message = "Cover image URL must not exceed 255 characters")
    private String coverImageUrl;

    private Integer status; // 0 = DRAFT, 1 = PUBLISHED, 2 = ARCHIVED

    @Size(max = 255, message = "SEO title must not exceed 255 characters")
    private String seoTitle;

    @Size(max = 255, message = "SEO description must not exceed 255 characters")
    private String seoDescription;

    @Size(max = 255, message = "SEO keywords must not exceed 255 characters")
    private String seoKeywords;

    // Status constants for convenience
    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_PUBLISHED = 1;
    public static final int STATUS_ARCHIVED = 2;

    // Helper methods

    /**
     * Check if any field is provided for update
     */
    public boolean hasUpdates() {
        return title != null || summary != null || content != null ||
                categoryId != null || tagIds != null || coverImageUrl != null ||
                status != null || seoTitle != null || seoDescription != null ||
                seoKeywords != null;
    }

    /**
     * Check if article has tags to update
     */
    public boolean hasTagUpdates() {
        return tagIds != null;
    }

    /**
     * Get tag count
     */
    public int getTagCount() {
        return tagIds != null ? tagIds.size() : 0;
    }

    /**
     * Check if title is being updated
     */
    public boolean isTitleUpdate() {
        return title != null && !title.trim().isEmpty();
    }

    /**
     * Check if status is being updated
     */
    public boolean isStatusUpdate() {
        return status != null;
    }

    /**
     * Check if content is being updated
     */
    public boolean isContentUpdate() {
        return content != null;
    }

    /**
     * Check if SEO fields are being updated
     */
    public boolean hasSeoUpdates() {
        return seoTitle != null || seoDescription != null || seoKeywords != null;
    }
}