package com.cdyt.be.dto.article;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class CreateArticleDto {

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 1000, message = "Summary must not exceed 1000 characters")
    private String summary;

    @NotBlank(message = "Content is required")
    private String content;

    private Long categoryId;

    private Set<Long> tagIds; // Tag IDs to associate with this article

    @Size(max = 255, message = "Cover image URL must not exceed 255 characters")
    private String coverImageUrl;

    private Integer status = 0; // 0 = DRAFT, 1 = PUBLISHED, 2 = ARCHIVED

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
     * Check if article has tags to assign
     */
    public boolean hasTags() {
        return tagIds != null && !tagIds.isEmpty();
    }

    /**
     * Get tag count
     */
    public int getTagCount() {
        return tagIds != null ? tagIds.size() : 0;
    }
}