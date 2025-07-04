package com.cdyt.be.repository;

import com.cdyt.be.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    // ========== BASIC FINDER METHODS ==========

    Optional<Article> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    /**
     * Find article by ID with author and category eagerly fetched
     */
    @Query("SELECT a FROM Article a " +
            "LEFT JOIN FETCH a.author " +
            "LEFT JOIN FETCH a.category " +
            "WHERE a.id = :id")
    Optional<Article> findByIdWithAuthorAndCategory(@Param("id") Long id);

    /**
     * Find article by slug with author and category eagerly fetched
     */
    @Query("SELECT a FROM Article a " +
            "LEFT JOIN FETCH a.author " +
            "LEFT JOIN FETCH a.category " +
            "WHERE a.slug = :slug")
    Optional<Article> findBySlugWithAuthorAndCategory(@Param("slug") String slug);

    // ========== PAGINATION AND SEARCH METHODS ==========

    /**
     * Find all articles with pagination and optional filters using native SQL
     * - If title is null: returns all articles (no title filtering)
     * - If status is null: returns ALL articles regardless of status
     * - If status is provided: filters by that specific status (0 = DRAFT, 1 =
     * PUBLISHED, 2 = ARCHIVED)
     * - If authorId is null: returns all articles (no author filtering)
     * - If categoryId is null: returns all articles (no category filtering)
     * - View count filters: applies when provided, otherwise no filtering
     */
    @Query(value = "SELECT * FROM article a WHERE " +
            "(:title IS NULL OR a.title ILIKE '%' || :title || '%') AND " +
            "(:status IS NULL OR a.status = :status) AND " +
            "(:authorId IS NULL OR a.author_id = :authorId) AND " +
            "(:categoryId IS NULL OR a.category_id = :categoryId) AND " +
            "(:minViewCount IS NULL OR a.view_count >= :minViewCount) AND " +
            "(:maxViewCount IS NULL OR a.view_count <= :maxViewCount) AND " +
            "a.is_deleted = false " +
            "ORDER BY a.created_at DESC, a.title ASC", nativeQuery = true)
    Page<Article> findArticlesWithFilters(
            @Param("title") String title,
            @Param("status") String status,
            @Param("authorId") Long authorId,
            @Param("categoryId") Long categoryId,
            @Param("minViewCount") Integer minViewCount,
            @Param("maxViewCount") Integer maxViewCount,
            Pageable pageable);

    /**
     * Find published articles only
     */
    List<Article> findByStatusAndIsDeletedFalseOrderByCreatedAtDescTitleAsc(Integer status);

    /**
     * Find all non-deleted articles
     */
    List<Article> findByIsDeletedFalseOrderByCreatedAtDescTitleAsc();

    /**
     * Search articles by title
     */
    List<Article> findByTitleContainingIgnoreCaseAndIsDeletedFalseOrderByCreatedAtDescTitleAsc(String title);

    /**
     * Find articles by author
     */
    List<Article> findByAuthor_IdAndIsDeletedFalseOrderByCreatedAtDescTitleAsc(Long authorId);

    /**
     * Find articles by category
     */
    List<Article> findByCategory_IdAndIsDeletedFalseOrderByCreatedAtDescTitleAsc(Long categoryId);

    /**
     * Find articles by multiple category IDs (for hierarchical search)
     */
    List<Article> findByCategory_IdInAndIsDeletedFalseOrderByCreatedAtDescTitleAsc(List<Long> categoryIds);

    /**
     * Find published articles by multiple category IDs (for public access)
     */
    List<Article> findByCategory_IdInAndStatusAndIsDeletedFalseOrderByCreatedAtDescTitleAsc(List<Long> categoryIds,
            Integer status);

    /**
     * Find popular articles (view count >= threshold)
     */
    List<Article> findByViewCountGreaterThanEqualAndIsDeletedFalseOrderByViewCountDescCreatedAtDesc(
            Integer threshold);

    /**
     * Find popular published articles (for public access)
     */
    List<Article> findByViewCountGreaterThanEqualAndStatusAndIsDeletedFalseOrderByViewCountDescCreatedAtDesc(
            Integer threshold, Integer status);

    /**
     * Find articles by status with pagination (for public access)
     */
    Page<Article> findByStatusAndIsDeletedFalse(Integer status, Pageable pageable);

    /**
     * Find published articles with filters (title search and date range)
     */
    @Query(value = "SELECT * FROM article a WHERE " +
            "a.status = 1 AND a.is_deleted = false AND " +
            "(:title IS NULL OR a.title ILIKE '%' || :title || '%') AND " +
            "(:fromDate IS NULL OR DATE(a.published_at) >= :fromDate) AND " +
            "(:toDate IS NULL OR DATE(a.published_at) <= :toDate) " +
            "ORDER BY a.published_at DESC, a.created_at DESC, a.title ASC", nativeQuery = true)
    Page<Article> findPublishedArticlesWithFilters(
            @Param("title") String title,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable);

    /**
     * Search published articles by title (for public access)
     */
    List<Article> findByTitleContainingIgnoreCaseAndStatusAndIsDeletedFalseOrderByCreatedAtDescTitleAsc(
            String title, Integer status);

    /**
     * Find published articles by tag with filters (title search and date range)
     */
    @Query(value = "SELECT DISTINCT a.* FROM article a " +
            "INNER JOIN article_tag at ON a.id = at.article_id " +
            "WHERE a.status = 1 AND a.is_deleted = false AND " +
            "at.tag_id = :tagId AND " +
            "(:title IS NULL OR a.title ILIKE '%' || :title || '%') AND " +
            "(:fromDate IS NULL OR DATE(a.published_at) >= :fromDate) AND " +
            "(:toDate IS NULL OR DATE(a.published_at) <= :toDate) " +
            "ORDER BY a.published_at DESC, a.created_at DESC, a.title ASC", nativeQuery = true)
    Page<Article> findPublishedArticlesByTagWithFilters(
            @Param("tagId") Long tagId,
            @Param("title") String title,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            Pageable pageable);

    // ========== STATISTICS ==========

    @Query(value = """
            SELECT
                COUNT(*) as totalArticles,
                COUNT(CASE WHEN status = 1 AND is_deleted = false THEN 1 END) as publishedArticles,
                COUNT(CASE WHEN status = 0 AND is_deleted = false THEN 1 END) as draftArticles,
                COUNT(CASE WHEN status = 2 AND is_deleted = false THEN 1 END) as archivedArticles,
                COALESCE(MAX(view_count), 0) as maxViewCount,
                COALESCE(AVG(view_count), 0) as avgViewCount
            FROM article
            WHERE is_deleted = false
            """, nativeQuery = true)
    Object[] getArticleStatistics();

    @Query(value = """
            SELECT
                COUNT(*) as totalPublishedArticles,
                COALESCE(MAX(view_count), 0) as maxViewCount,
                COALESCE(AVG(view_count), 0) as avgViewCount
            FROM article
            WHERE status = 1 AND is_deleted = false
            """, nativeQuery = true)
    Object[] getPublishedArticleStatistics();

    @Modifying
    @Query(value = "UPDATE article SET view_count = view_count + :increment WHERE id = :id", nativeQuery = true)
    void incrementViewCount(@Param("id") Long id, @Param("increment") Long increment);
}