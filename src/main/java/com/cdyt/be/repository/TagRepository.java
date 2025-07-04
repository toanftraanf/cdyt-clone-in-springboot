package com.cdyt.be.repository;

import com.cdyt.be.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

        // ========== BASIC FINDER METHODS ==========

        Optional<Tag> findBySlug(String slug);

        boolean existsBySlug(String slug);

        boolean existsBySlugAndIdNot(String slug, Long id);

        // ========== PAGINATION AND SEARCH METHODS ==========

        /**
         * Find all tags with pagination and optional filters using native SQL
         * - If name is null: returns all tags (no name filtering)
         * - If status is null: returns ALL tags regardless of status (active +
         * inactive)
         * - If status is provided: filters by that specific status (1=active,
         * 0=inactive)
         * - If color is null: returns all tags (no color filtering)
         * - Usage count filters: applies when provided, otherwise no filtering
         */
        @Query(value = "SELECT * FROM tags t WHERE " +
                        "(:name IS NULL OR t.name ILIKE '%' || :name || '%') AND " +
                        "(:status IS NULL OR t.status = :status) AND " +
                        "(:color IS NULL OR t.color = :color) AND " +
                        "(:minUsageCount IS NULL OR t.usage_count >= :minUsageCount) AND " +
                        "(:maxUsageCount IS NULL OR t.usage_count <= :maxUsageCount) AND " +
                        "t.is_deleted = false " +
                        "ORDER BY t.display_order ASC, t.name ASC", nativeQuery = true)
        Page<Tag> findTagsWithFilters(
                        @Param("name") String name,
                        @Param("status") Integer status,
                        @Param("color") String color,
                        @Param("minUsageCount") Long minUsageCount,
                        @Param("maxUsageCount") Long maxUsageCount,
                        Pageable pageable);

        /**
         * Find active tags only
         */
        List<Tag> findByStatusAndIsDeletedFalseOrderByDisplayOrderAscNameAsc(Integer status);

        /**
         * Find all non-deleted tags
         */
        List<Tag> findByIsDeletedFalseOrderByDisplayOrderAscNameAsc();

        /**
         * Search tags by name
         */
        List<Tag> findByNameContainingIgnoreCaseAndIsDeletedFalseOrderByUsageCountDescNameAsc(String name);

        /**
         * Find popular tags (usage count >= threshold)
         */
        List<Tag> findByUsageCountGreaterThanEqualAndIsDeletedFalseOrderByUsageCountDescNameAsc(Long threshold);

        /**
         * Find unused tags
         */
        List<Tag> findByUsageCountAndIsDeletedFalseOrderByCreatedAtDesc(Long usageCount);

        /**
         * Find tags by color
         */
        List<Tag> findByColorAndIsDeletedFalseOrderByNameAsc(String color);

        // ========== TOP USED TAGS ==========

        @Query("SELECT t FROM Tag t WHERE t.isDeleted = false AND t.status = 1 ORDER BY t.usageCount DESC, t.name ASC")
        List<Tag> findTopUsedTags(Pageable pageable);

        // ========== STATISTICS ==========

        @Query(value = """
                        SELECT
                            COUNT(*) as totalTags,
                            COUNT(CASE WHEN status = 1 AND is_deleted = false THEN 1 END) as activeTags,
                            COUNT(CASE WHEN usage_count = 0 AND is_deleted = false THEN 1 END) as unusedTags,
                            COALESCE(MAX(usage_count), 0) as maxUsageCount,
                            COALESCE(AVG(usage_count), 0) as avgUsageCount
                        FROM tags
                        WHERE is_deleted = false
                        """, nativeQuery = true)
        Object[] getTagStatistics();

        // ========== USAGE COUNT MANAGEMENT ==========

        /**
         * Increment usage count for a tag
         */
        @Modifying
        @Query("UPDATE Tag t SET t.usageCount = t.usageCount + 1 WHERE t.id = :tagId")
        void incrementUsageCount(@Param("tagId") Long tagId);

        /**
         * Decrement usage count for a tag (with minimum 0)
         */
        @Modifying
        @Query("UPDATE Tag t SET t.usageCount = CASE WHEN t.usageCount > 0 THEN t.usageCount - 1 ELSE 0 END WHERE t.id = :tagId")
        void decrementUsageCount(@Param("tagId") Long tagId);
}