package com.cdyt.be.repository;

import com.cdyt.be.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

        // ========== BASIC FINDER METHODS ==========

        Optional<Category> findBySlug(String slug);

        boolean existsBySlug(String slug);

        boolean existsBySlugAndIdNot(String slug, Long id);

        // ========== PAGINATION AND SEARCH METHODS ==========

        /**
         * Find all categories with pagination and optional filters using native SQL
         * - If name is null: returns all categories (no name filtering)
         * - If status is null: returns ALL categories regardless of status (active +
         * inactive)
         * - If status is provided: filters by that specific status (1=active,
         * 0=inactive)
         */
        @Query(value = "SELECT * FROM categories c WHERE " +
                        "(:name IS NULL OR c.name ILIKE '%' || :name || '%') AND " +
                        "(:status IS NULL OR c.status = :status) AND " +
                        "c.is_deleted = false " +
                        "ORDER BY c.display_order ASC, c.name ASC", nativeQuery = true)
        Page<Category> findCategoriesWithFilters(
                        @Param("name") String name,
                        @Param("status") Integer status,
                        Pageable pageable);

        /**
         * Find active categories only
         */
        List<Category> findByStatusAndIsDeletedFalseOrderByDisplayOrderAscNameAsc(Integer status);

        /**
         * Find root categories (no parent)
         */
        List<Category> findByParentIsNullAndIsDeletedFalseOrderByDisplayOrderAscNameAsc();

        /**
         * Find children of a specific category
         */
        List<Category> findByParentIdAndIsDeletedFalseOrderByDisplayOrderAscNameAsc(Long parentId);

        /**
         * Search categories by name
         */
        List<Category> findByNameContainingIgnoreCaseAndIsDeletedFalseOrderByNameAsc(String name);

        // ========== HIERARCHY METHODS ==========

        @Query("SELECT COUNT(c) FROM Category c WHERE c.parent.id = :parentId AND c.isDeleted = false")
        long countChildrenByParentId(@Param("parentId") Long parentId);

        @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId AND c.isDeleted = false ORDER BY c.displayOrder ASC")
        List<Category> findChildrenByParentId(@Param("parentId") Long parentId);

        /**
         * Find all descendant category IDs (including the category itself) using
         * recursive CTE
         */
        @Query(value = """
                        WITH RECURSIVE category_tree AS (
                            -- Base case: start with the given category
                            SELECT id, parent_id, name
                            FROM categories
                            WHERE id = :categoryId AND is_deleted = false

                            UNION ALL

                            -- Recursive case: find children
                            SELECT c.id, c.parent_id, c.name
                            FROM categories c
                            INNER JOIN category_tree ct ON c.parent_id = ct.id
                            WHERE c.is_deleted = false
                        )
                        SELECT id FROM category_tree
                        """, nativeQuery = true)
        List<Long> findAllDescendantIds(@Param("categoryId") Long categoryId);

        // ========== STATISTICS ==========

        @Query(value = """
                        SELECT
                            COUNT(*) as totalCategories,
                            COUNT(CASE WHEN status = 1 AND is_deleted = false THEN 1 END) as activeCategories,
                            COUNT(CASE WHEN parent_id IS NULL AND is_deleted = false THEN 1 END) as rootCategories
                        FROM categories
                        WHERE is_deleted = false
                        """, nativeQuery = true)
        Object[] getCategoryStatistics();
}