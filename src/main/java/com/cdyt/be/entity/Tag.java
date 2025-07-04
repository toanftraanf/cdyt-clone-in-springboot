package com.cdyt.be.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tags")
@Data
@EqualsAndHashCode(callSuper = false)
@EntityListeners(AuditingEntityListener.class)
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 255)
    private String slug;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "color", length = 7)
    private String color;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "status", nullable = false)
    private Integer status = 1; // 1 = active, 0 = inactive

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "usage_count", nullable = false)
    private Long usageCount = 0L; // Track how many times this tag is used

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Convenience methods

    /**
     * Check if tag is active
     */
    public boolean isActive() {
        return status == 1 && !isDeleted;
    }

    /**
     * Check if tag is popular (usage count > threshold)
     */
    public boolean isPopular(long threshold) {
        return usageCount >= threshold;
    }

    /**
     * Increment usage count
     */
    public void incrementUsage() {
        this.usageCount++;
    }

    /**
     * Decrement usage count (ensure it doesn't go negative)
     */
    public void decrementUsage() {
        this.usageCount = Math.max(0, this.usageCount - 1);
    }

    /**
     * Get tag display name with usage count
     */
    public String getDisplayNameWithCount() {
        return String.format("%s (%d)", name, usageCount);
    }

    /**
     * Check if tag has custom color
     */
    public boolean hasCustomColor() {
        return color != null && !color.trim().isEmpty();
    }

    /**
     * Get color or default
     */
    public String getColorOrDefault(String defaultColor) {
        return hasCustomColor() ? color : defaultColor;
    }
}