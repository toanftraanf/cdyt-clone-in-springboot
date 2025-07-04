package com.cdyt.be.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "article")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "slug", nullable = false, unique = true, length = 255)
    private String slug;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", referencedColumnName = "id")
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id", nullable = true)
    private Category category;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "article_tag", joinColumns = @JoinColumn(name = "article_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new HashSet<>();

    @Column(name = "cover_image_url", length = 255)
    private String coverImageUrl;

    @Column(name = "status", nullable = false)
    private Integer status = 0; // 0 = DRAFT, 1 = PUBLISHED, 2 = ARCHIVED

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Column(name = "comment_count", nullable = false)
    private Integer commentCount = 0;

    @Column(name = "seo_title", length = 255)
    private String seoTitle;

    @Column(name = "seo_description", length = 255)
    private String seoDescription;

    @Column(name = "seo_keywords", length = 255)
    private String seoKeywords;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Status constants
    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_PUBLISHED = 1;
    public static final int STATUS_ARCHIVED = 2;

    // Convenience methods

    /**
     * Check if article is published
     */
    public boolean isPublished() {
        return status != null && status == STATUS_PUBLISHED && !isDeleted;
    }

    /**
     * Check if article is draft
     */
    public boolean isDraft() {
        return status != null && status == STATUS_DRAFT && !isDeleted;
    }

    /**
     * Check if article is archived
     */
    public boolean isArchived() {
        return status != null && status == STATUS_ARCHIVED && !isDeleted;
    }

    /**
     * Get status as string
     */
    public String getStatusAsString() {
        if (status == null)
            return "UNKNOWN";
        return switch (status) {
            case STATUS_DRAFT -> "DRAFT";
            case STATUS_PUBLISHED -> "PUBLISHED";
            case STATUS_ARCHIVED -> "ARCHIVED";
            default -> "UNKNOWN";
        };
    }

    /**
     * Set status from string
     */
    public void setStatusFromString(String statusString) {
        if (statusString == null) {
            this.status = STATUS_DRAFT;
            return;
        }
        this.status = switch (statusString.toUpperCase()) {
            case "DRAFT" -> STATUS_DRAFT;
            case "PUBLISHED" -> STATUS_PUBLISHED;
            case "ARCHIVED" -> STATUS_ARCHIVED;
            default -> STATUS_DRAFT;
        };
    }

    /**
     * Increment view count
     */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /**
     * Increment like count
     */
    public void incrementLikeCount() {
        this.likeCount++;
    }

    /**
     * Decrement like count (ensure it doesn't go negative)
     */
    public void decrementLikeCount() {
        this.likeCount = Math.max(0, this.likeCount - 1);
    }

    /**
     * Increment comment count
     */
    public void incrementCommentCount() {
        this.commentCount++;
    }

    /**
     * Decrement comment count (ensure it doesn't go negative)
     */
    public void decrementCommentCount() {
        this.commentCount = Math.max(0, this.commentCount - 1);
    }

    /**
     * Get display name with view count
     */
    public String getDisplayNameWithViews() {
        return String.format("%s (%d views)", title, viewCount);
    }

    /**
     * Check if article has cover image
     */
    public boolean hasCoverImage() {
        return coverImageUrl != null && !coverImageUrl.trim().isEmpty();
    }

    /**
     * Get cover image or default placeholder
     */
    public String getCoverImageOrDefault(String defaultImageUrl) {
        return hasCoverImage() ? coverImageUrl : defaultImageUrl;
    }

    // Tag management convenience methods

    /**
     * Add a tag to this article
     */
    public void addTag(Tag tag) {
        if (tag != null) {
            this.tags.add(tag);
        }
    }

    /**
     * Remove a tag from this article
     */
    public void removeTag(Tag tag) {
        if (tag != null) {
            this.tags.remove(tag);
        }
    }

    /**
     * Check if article has any tags
     */
    public boolean hasTags() {
        return tags != null && !tags.isEmpty();
    }

    /**
     * Get tag count
     */
    public int getTagCount() {
        return tags != null ? tags.size() : 0;
    }

    /**
     * Clear all tags
     */
    public void clearTags() {
        if (tags != null) {
            tags.clear();
        }
    }

    /**
     * Set tags (replaces all existing tags)
     */
    public void setTags(Set<Tag> newTags) {
        if (this.tags == null) {
            this.tags = new HashSet<>();
        }
        this.tags.clear();
        if (newTags != null) {
            this.tags.addAll(newTags);
        }
    }
}