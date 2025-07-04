package com.cdyt.be.service;

import com.cdyt.be.common.context.UserContextHolder;
import com.cdyt.be.common.exception.BusinessException;
import com.cdyt.be.dto.article.ArticleDetailResponseDto;
import com.cdyt.be.dto.article.ArticleResponseDto;
import com.cdyt.be.dto.article.ArticleSearchRequestDto;
import com.cdyt.be.dto.article.ArticlesByTagSearchRequestDto;
import com.cdyt.be.dto.article.CreateArticleDto;
import com.cdyt.be.dto.article.PublicArticleSearchRequestDto;
import com.cdyt.be.dto.article.UpdateArticleDto;
import com.cdyt.be.entity.Article;
import com.cdyt.be.entity.Category;
import com.cdyt.be.entity.Tag;
import com.cdyt.be.entity.User;
import com.cdyt.be.mapper.ArticleMapper;
import com.cdyt.be.repository.ArticleRepository;
import com.cdyt.be.repository.CategoryRepository;
import com.cdyt.be.repository.TagRepository;
import com.cdyt.be.util.TextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.time.Duration;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final StringRedisTemplate redisTemplate;
    private static final String VIEW_KEY_PREFIX = "article:view:";
    private static final String UNIQUE_VIEW_KEY_PREFIX = "viewed:";
    private static final Duration UNIQUE_TTL = Duration.ofHours(2);

    /**
     * Create a new article with automatic tag usage tracking
     */
    @Transactional
    public ArticleResponseDto createArticle(CreateArticleDto createDto) {
        Article article = articleMapper.toEntity(createDto);

        // Set author from current user context
        User currentUser = UserContextHolder.getCurrentUser();
        if (currentUser != null) {
            article.setAuthor(currentUser);
        } else {
            throw BusinessException.unauthorized("User context not found. Please ensure you are authenticated.");
        }

        // Generate unique slug from title
        String baseSlug = TextUtils.generateSlug(createDto.getTitle());
        String uniqueSlug = generateUniqueSlug(baseSlug);
        article.setSlug(uniqueSlug);

        // Handle tags if provided
        if (createDto.hasTags()) {
            Set<Tag> tags = loadAndValidateTags(createDto.getTagIds());
            article.setTags(tags);
        }

        // Set publishedAt if status is PUBLISHED
        if (article.getStatus() != null && article.getStatus() == Article.STATUS_PUBLISHED) {
            article.setPublishedAt(LocalDateTime.now());
        }

        // Save article first
        Article savedArticle = articleRepository.save(article);

        // Automatically increment tag usage counts for associated tags
        if (savedArticle.hasTags()) {
            incrementTagUsageForArticle(savedArticle);
            log.info("Incremented usage count for {} tags in article: {}",
                    savedArticle.getTagCount(), savedArticle.getTitle());
        }

        return articleMapper.toResponseDto(savedArticle);
    }

    /**
     * Search articles with pagination and optional filters using request DTO
     * 
     * Filter behavior:
     * - title = null: Returns all articles (no title filtering)
     * - status = null: Returns ALL articles (draft, published, archived)
     * - status = "0": Returns only draft articles
     * - status = "1": Returns only published articles
     * - status = "2": Returns only archived articles
     * - authorId = null: Returns all articles (no author filtering)
     * - categoryId = null: Returns all articles (no category filtering)
     * - View count filters: applied when provided
     */
    public Page<ArticleResponseDto> searchArticles(ArticleSearchRequestDto searchRequest) {
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());
        Page<Article> articles = articleRepository.findArticlesWithFilters(
                searchRequest.getCleanTitle(),
                searchRequest.getCleanStatus(),
                searchRequest.getAuthorId(),
                searchRequest.getCategoryId(),
                searchRequest.getMinViewCount(),
                searchRequest.getMaxViewCount(),
                pageable);

        List<ArticleResponseDto> articleDtos = articleMapper.toResponseDtoList(articles.getContent());
        return new PageImpl<>(articleDtos, pageable, articles.getTotalElements());
    }

    /**
     * Get all published articles
     */
    public List<ArticleResponseDto> getAllPublishedArticles() {
        List<Article> articles = articleRepository
                .findByStatusAndIsDeletedFalseOrderByCreatedAtDescTitleAsc(Article.STATUS_PUBLISHED);
        return articleMapper.toResponseDtoList(articles);
    }

    /**
     * Get all non-deleted articles
     */
    public List<ArticleResponseDto> getAllNonDeletedArticles() {
        List<Article> articles = articleRepository.findByIsDeletedFalseOrderByCreatedAtDescTitleAsc();
        return articleMapper.toResponseDtoList(articles);
    }

    /**
     * Get article by ID with full author and category information
     */
    public Optional<ArticleDetailResponseDto> getArticleById(Long id) {
        return articleRepository.findByIdWithAuthorAndCategory(id)
                .filter(article -> !article.getIsDeleted())
                .map(articleMapper::toDetailResponseDto);
    }

    /**
     * Get article by slug with full author and category information
     */
    public Optional<ArticleDetailResponseDto> getArticleBySlug(String slug) {
        return articleRepository.findBySlugWithAuthorAndCategory(slug)
                .filter(article -> !article.getIsDeleted())
                .map(articleMapper::toDetailResponseDto);
    }

    /**
     * Search articles by title
     */
    public List<ArticleResponseDto> searchArticlesByTitle(String title) {
        List<Article> articles = articleRepository
                .findByTitleContainingIgnoreCaseAndIsDeletedFalseOrderByCreatedAtDescTitleAsc(title);
        return articleMapper.toResponseDtoList(articles);
    }

    /**
     * Get articles by author
     */
    public List<ArticleResponseDto> getArticlesByAuthor(Long authorId) {
        List<Article> articles = articleRepository
                .findByAuthor_IdAndIsDeletedFalseOrderByCreatedAtDescTitleAsc(authorId);
        return articleMapper.toResponseDtoList(articles);
    }

    /**
     * Get articles by category (including all child categories)
     */
    public List<ArticleResponseDto> getArticlesByCategory(Long categoryId) {
        // Get all descendant category IDs (including the category itself)
        List<Long> categoryIds = categoryRepository.findAllDescendantIds(categoryId);

        // If no categories found (category doesn't exist or is deleted), return empty
        // list
        if (categoryIds.isEmpty()) {
            return new ArrayList<>();
        }

        // Find articles in the category and all its descendants
        List<Article> articles = articleRepository
                .findByCategory_IdInAndIsDeletedFalseOrderByCreatedAtDescTitleAsc(categoryIds);
        return articleMapper.toResponseDtoList(articles);
    }

    /**
     * Get popular articles (view count >= threshold)
     */
    public List<ArticleResponseDto> getPopularArticles(Integer threshold) {
        if (threshold == null || threshold < 0) {
            threshold = 100; // Default threshold
        }
        List<Article> articles = articleRepository
                .findByViewCountGreaterThanEqualAndIsDeletedFalseOrderByViewCountDescCreatedAtDesc(threshold);
        return articleMapper.toResponseDtoList(articles);
    }

    /**
     * Get article statistics
     */
    public ArticleStats getArticleStats() {
        Object[] stats = articleRepository.getArticleStatistics();
        if (stats != null && stats.length >= 6) {
            return new ArticleStats(
                    ((Number) stats[0]).longValue(), // totalArticles
                    ((Number) stats[1]).longValue(), // publishedArticles
                    ((Number) stats[2]).longValue(), // draftArticles
                    ((Number) stats[3]).longValue(), // archivedArticles
                    ((Number) stats[4]).intValue(), // maxViewCount
                    ((Number) stats[5]).doubleValue() // avgViewCount
            );
        }
        return new ArticleStats(0L, 0L, 0L, 0L, 0, 0.0);
    }

    /**
     * Article statistics inner class
     */
    public static class ArticleStats {
        public final long totalArticles;
        public final long publishedArticles;
        public final long draftArticles;
        public final long archivedArticles;
        public final int maxViewCount;
        public final double avgViewCount;

        public ArticleStats(long totalArticles, long publishedArticles, long draftArticles,
                long archivedArticles, int maxViewCount, double avgViewCount) {
            this.totalArticles = totalArticles;
            this.publishedArticles = publishedArticles;
            this.draftArticles = draftArticles;
            this.archivedArticles = archivedArticles;
            this.maxViewCount = maxViewCount;
            this.avgViewCount = avgViewCount;
        }
    }

    /**
     * Generate a unique slug by appending numbers if needed
     */
    private String generateUniqueSlug(String baseSlug) {
        return generateUniqueSlug(baseSlug, null);
    }

    /**
     * Generate a unique slug by appending numbers if needed, excluding a specific
     * ID
     */
    private String generateUniqueSlug(String baseSlug, Long excludeId) {
        String slug = baseSlug;
        int counter = 1;

        while (excludeId != null
                ? articleRepository.existsBySlugAndIdNot(slug, excludeId)
                : articleRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }

        return slug;
    }

    // ========== PUBLIC ACCESS METHODS (PUBLISHED ARTICLES ONLY) ==========

    /**
     * Get published articles with pagination (for public access)
     */
    public Page<ArticleResponseDto> getPublishedArticlesPaginated(Pageable pageable) {
        Page<Article> articles = articleRepository.findByStatusAndIsDeletedFalse(Article.STATUS_PUBLISHED, pageable);
        List<ArticleResponseDto> articleDtos = articleMapper.toResponseDtoList(articles.getContent());
        return new PageImpl<>(articleDtos, pageable, articles.getTotalElements());
    }

    /**
     * Get published articles with filters (for public access)
     * Supports pagination, title search, and date range filtering
     */
    public Page<ArticleResponseDto> getPublishedArticlesWithFilters(Pageable pageable, String title,
            String fromDate, String toDate) {

        // Parse date strings if provided
        LocalDate fromLocalDate = null;
        LocalDate toLocalDate = null;

        if (fromDate != null && !fromDate.trim().isEmpty()) {
            try {
                fromLocalDate = LocalDate.parse(fromDate, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid fromDate format. Use YYYY-MM-DD format.");
            }
        }

        if (toDate != null && !toDate.trim().isEmpty()) {
            try {
                toLocalDate = LocalDate.parse(toDate, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid toDate format. Use YYYY-MM-DD format.");
            }
        }

        // Clean title parameter
        String cleanTitle = (title != null && !title.trim().isEmpty()) ? title.trim() : null;

        Page<Article> articles = articleRepository.findPublishedArticlesWithFilters(
                cleanTitle, fromLocalDate, toLocalDate, pageable);
        List<ArticleResponseDto> articleDtos = articleMapper.toResponseDtoList(articles.getContent());
        return new PageImpl<>(articleDtos, pageable, articles.getTotalElements());
    }

    /**
     * Get published articles with filters using request DTO (for public access)
     */
    public Page<ArticleResponseDto> getPublishedArticlesWithFilters(PublicArticleSearchRequestDto searchRequest) {
        // Validate request
        if (!searchRequest.isValidPage()) {
            throw new IllegalArgumentException("Invalid page number. Must be >= 0.");
        }
        if (!searchRequest.isValidSize()) {
            throw new IllegalArgumentException("Invalid page size. Must be between 1 and 100.");
        }

        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());
        return getPublishedArticlesWithFilters(pageable, searchRequest.getTitle(),
                searchRequest.getFromDate(), searchRequest.getToDate());
    }

    /**
     * Get published article by slug with full details (for public access)
     */
    public Optional<ArticleDetailResponseDto> getPublishedArticleBySlug(String slug) {
        return articleRepository.findBySlugWithAuthorAndCategory(slug)
                .filter(article -> !article.getIsDeleted() && article.getStatus() == Article.STATUS_PUBLISHED)
                .map(articleMapper::toDetailResponseDto);
    }

    /**
     * Get published article by ID with full details (for public access)
     */
    public Optional<ArticleDetailResponseDto> getPublishedArticleById(Long id) {
        return articleRepository.findByIdWithAuthorAndCategory(id)
                .filter(article -> !article.getIsDeleted() && article.getStatus() == Article.STATUS_PUBLISHED)
                .map(articleMapper::toDetailResponseDto);
    }

    /**
     * Get published articles by category (for public access)
     */
    public List<ArticleResponseDto> getPublishedArticlesByCategory(Long categoryId) {
        // Get all descendant category IDs (including the category itself)
        List<Long> categoryIds = categoryRepository.findAllDescendantIds(categoryId);

        if (categoryIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Article> articles = articleRepository
                .findByCategory_IdInAndStatusAndIsDeletedFalseOrderByCreatedAtDescTitleAsc(
                        categoryIds, Article.STATUS_PUBLISHED);
        return articleMapper.toResponseDtoList(articles);
    }

    /**
     * Get popular published articles (for public access)
     */
    public List<ArticleResponseDto> getPopularPublishedArticles(Integer threshold) {
        if (threshold == null || threshold < 0) {
            threshold = 100;
        }
        List<Article> articles = articleRepository
                .findByViewCountGreaterThanEqualAndStatusAndIsDeletedFalseOrderByViewCountDescCreatedAtDesc(
                        threshold, Article.STATUS_PUBLISHED);
        return articleMapper.toResponseDtoList(articles);
    }

    /**
     * Search published articles by title (for public access)
     */
    public List<ArticleResponseDto> searchPublishedArticlesByTitle(String title) {
        List<Article> articles = articleRepository
                .findByTitleContainingIgnoreCaseAndStatusAndIsDeletedFalseOrderByCreatedAtDescTitleAsc(
                        title, Article.STATUS_PUBLISHED);
        return articleMapper.toResponseDtoList(articles);
    }

    /**
     * Get published article statistics (for public access)
     */
    public Map<String, Object> getPublishedArticleStats() {
        Object[] stats = articleRepository.getPublishedArticleStatistics();
        if (stats != null && stats.length >= 3) {
            return Map.of(
                    "totalPublishedArticles", ((Number) stats[0]).longValue(),
                    "maxViewCount", ((Number) stats[1]).intValue(),
                    "avgViewCount", ((Number) stats[2]).doubleValue());
        }
        return Map.of("totalPublishedArticles", 0L, "maxViewCount", 0, "avgViewCount", 0.0);
    }

    /**
     * Get published articles by tag with pagination, search, and date filtering
     */
    public Page<ArticleResponseDto> getPublishedArticlesByTag(ArticlesByTagSearchRequestDto searchRequest) {
        // Validate request
        if (!searchRequest.isValidPage()) {
            throw new IllegalArgumentException("Invalid page number. Must be >= 0.");
        }
        if (!searchRequest.isValidSize()) {
            throw new IllegalArgumentException("Invalid page size. Must be between 1 and 100.");
        }

        // Parse date strings if provided
        LocalDate fromLocalDate = null;
        LocalDate toLocalDate = null;

        if (searchRequest.getFromDate() != null && !searchRequest.getFromDate().trim().isEmpty()) {
            try {
                fromLocalDate = LocalDate.parse(searchRequest.getFromDate(), DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid fromDate format. Use YYYY-MM-DD format.");
            }
        }

        if (searchRequest.getToDate() != null && !searchRequest.getToDate().trim().isEmpty()) {
            try {
                toLocalDate = LocalDate.parse(searchRequest.getToDate(), DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid toDate format. Use YYYY-MM-DD format.");
            }
        }

        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());

        Page<Article> articles = articleRepository.findPublishedArticlesByTagWithFilters(
                searchRequest.getTagId(),
                searchRequest.getCleanTitle(),
                fromLocalDate,
                toLocalDate,
                pageable);

        return articles.map(articleMapper::toResponseDto);
    }

    // ========== AUTOMATIC TAG USAGE TRACKING ==========

    /**
     * Load and validate tags by IDs
     */
    private Set<Tag> loadAndValidateTags(Set<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new HashSet<>();
        }

        Set<Tag> tags = new HashSet<>();
        for (Long tagId : tagIds) {
            Tag tag = tagRepository.findById(tagId)
                    .orElseThrow(() -> BusinessException.notFound("Tag", tagId));

            if (tag.getIsDeleted()) {
                throw BusinessException.invalidState("Cannot use deleted tag with ID: " + tagId);
            }

            tags.add(tag);
        }
        return tags;
    }

    /**
     * Increment tag usage count for all tags in an article
     */
    private void incrementTagUsageForArticle(Article article) {
        if (article.hasTags()) {
            for (Tag tag : article.getTags()) {
                try {
                    tagRepository.incrementUsageCount(tag.getId());
                } catch (Exception e) {
                    log.warn("Failed to increment usage for tag {}: {}", tag.getName(), e.getMessage());
                }
            }
        }
    }

    /**
     * Decrement tag usage count for all tags in an article
     */
    private void decrementTagUsageForArticle(Article article) {
        if (article.hasTags()) {
            for (Tag tag : article.getTags()) {
                try {
                    tagRepository.decrementUsageCount(tag.getId());
                } catch (Exception e) {
                    log.warn("Failed to decrement usage for tag {}: {}", tag.getName(), e.getMessage());
                }
            }
        }
    }

    /**
     * Update tag usage when article tags change
     */
    private void updateTagUsageForChangedTags(Set<Tag> oldTags, Set<Tag> newTags) {
        // Find tags to remove (decrement usage)
        Set<Tag> tagsToRemove = new HashSet<>(oldTags);
        tagsToRemove.removeAll(newTags);

        // Find tags to add (increment usage)
        Set<Tag> tagsToAdd = new HashSet<>(newTags);
        tagsToAdd.removeAll(oldTags);

        // Decrement usage for removed tags
        for (Tag tag : tagsToRemove) {
            try {
                tagRepository.decrementUsageCount(tag.getId());
                log.debug("Decremented usage for removed tag: {}", tag.getName());
            } catch (Exception e) {
                log.warn("Failed to decrement usage for removed tag {}: {}", tag.getName(), e.getMessage());
            }
        }

        // Increment usage for added tags
        for (Tag tag : tagsToAdd) {
            try {
                tagRepository.incrementUsageCount(tag.getId());
                log.debug("Incremented usage for added tag: {}", tag.getName());
            } catch (Exception e) {
                log.warn("Failed to increment usage for added tag {}: {}", tag.getName(), e.getMessage());
            }
        }

        if (!tagsToRemove.isEmpty() || !tagsToAdd.isEmpty()) {
            log.info("Updated tag usage: {} tags removed, {} tags added",
                    tagsToRemove.size(), tagsToAdd.size());
        }
    }

    // ========== ARTICLE UPDATE AND DELETE WITH AUTOMATIC TAG TRACKING ==========

    /**
     * Update an existing article with automatic tag usage tracking
     */
    @Transactional
    public ArticleResponseDto updateArticle(Long id, UpdateArticleDto updateDto) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Article", id));
        if (article.getIsDeleted()) {
            throw BusinessException.invalidState("Cannot update deleted article");
        }

        if (!updateDto.hasUpdates()) {
            throw BusinessException.invalidInput("No updates provided");
        }

        // Store old tags before updating (only if tags are being updated)
        Set<Tag> oldTags = new HashSet<>();
        if (updateDto.hasTagUpdates()) {
            oldTags = new HashSet<>(article.getTags());
        }

        // Update basic fields only if provided
        if (updateDto.isTitleUpdate()) {
            article.setTitle(updateDto.getTitle());
            // Regenerate slug if title changed
            String baseSlug = TextUtils.generateSlug(updateDto.getTitle());
            String uniqueSlug = generateUniqueSlug(baseSlug, id);
            article.setSlug(uniqueSlug);
            log.info("Updated article title and regenerated slug: {}", uniqueSlug);
        }

        if (updateDto.getSummary() != null) {
            article.setSummary(updateDto.getSummary());
        }

        if (updateDto.isContentUpdate()) {
            article.setContent(updateDto.getContent());
        }

        if (updateDto.getCoverImageUrl() != null) {
            article.setCoverImageUrl(updateDto.getCoverImageUrl());
        }

        // Handle category update
        if (updateDto.getCategoryId() != null) {
            if (updateDto.getCategoryId() == 0) {
                // Remove category (set to null)
                article.setCategory(null);
            } else {
                // Set new category (validation handled by FK constraint)
                Category category = new Category();
                category.setId(updateDto.getCategoryId());
                article.setCategory(category);
            }
        }

        // Handle SEO field updates
        if (updateDto.hasSeoUpdates()) {
            if (updateDto.getSeoTitle() != null) {
                article.setSeoTitle(updateDto.getSeoTitle());
            }
            if (updateDto.getSeoDescription() != null) {
                article.setSeoDescription(updateDto.getSeoDescription());
            }
            if (updateDto.getSeoKeywords() != null) {
                article.setSeoKeywords(updateDto.getSeoKeywords());
            }
        }

        // Handle tag updates with automatic usage tracking
        if (updateDto.hasTagUpdates()) {
            Set<Tag> newTags = loadAndValidateTags(updateDto.getTagIds());
            updateTagUsageForChangedTags(oldTags, newTags);
            article.setTags(newTags);
            log.info("Updated tags for article '{}': {} old tags, {} new tags",
                    article.getTitle(), oldTags.size(), newTags.size());
        }

        // Handle status change with published date logic
        if (updateDto.isStatusUpdate()) {
            Integer oldStatus = article.getStatus();
            article.setStatus(updateDto.getStatus());

            // Set publishedAt when changing from non-published to published
            if (oldStatus != Article.STATUS_PUBLISHED &&
                    updateDto.getStatus() == Article.STATUS_PUBLISHED) {
                article.setPublishedAt(LocalDateTime.now());
                log.info("Article '{}' status changed to PUBLISHED with publishedAt set", article.getTitle());
            }
            // Clear publishedAt when changing from published to draft/archived
            else if (oldStatus == Article.STATUS_PUBLISHED &&
                    updateDto.getStatus() != Article.STATUS_PUBLISHED) {
                article.setPublishedAt(null);
                log.info("Article '{}' status changed from PUBLISHED, publishedAt cleared", article.getTitle());
            }
        }

        Article savedArticle = articleRepository.save(article);
        log.info("Successfully updated article with ID: {}", id);
        return articleMapper.toResponseDto(savedArticle);
    }

    /**
     * Delete an article with automatic tag usage tracking (soft delete)
     */
    @Transactional
    public void deleteArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Article", id));

        if (article.getIsDeleted()) {
            throw BusinessException.invalidState("Article is already deleted");
        }

        // Decrement tag usage counts before deleting
        if (article.hasTags()) {
            decrementTagUsageForArticle(article);
            log.info("Decremented usage count for {} tags in deleted article: {}",
                    article.getTagCount(), article.getTitle());
        }

        // Soft delete the article
        article.setIsDeleted(true);
        articleRepository.save(article);
    }

    /**
     * Restore a deleted article with automatic tag usage tracking
     */
    @Transactional
    public ArticleResponseDto restoreArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Article", id));

        if (!article.getIsDeleted()) {
            throw BusinessException.invalidState("Article is not deleted");
        }

        // Restore the article
        article.setIsDeleted(false);
        Article savedArticle = articleRepository.save(article);

        // Increment tag usage counts after restoring
        if (savedArticle.hasTags()) {
            incrementTagUsageForArticle(savedArticle);
            log.info("Incremented usage count for {} tags in restored article: {}",
                    savedArticle.getTagCount(), savedArticle.getTitle());
        }

        return articleMapper.toResponseDto(savedArticle);
    }

    private static final long RATE_LIMIT = 60; // max views per IP per minute

    public void recordView(Long articleId, String ipAddress) {
        if (articleId == null || ipAddress == null || ipAddress.isBlank())
            return;

        // Simple rate-limiting to hạn chế spam/bot
        String rateKey = "rate:" + ipAddress;
        Long reqs = redisTemplate.opsForValue().increment(rateKey);
        if (reqs != null && reqs == 1L) {
            redisTemplate.expire(rateKey, Duration.ofMinutes(1));
        }
        // Nếu quá ngưỡng, bỏ qua view (không tăng)
        if (reqs != null && reqs > RATE_LIMIT) {
            return;
        }

        String uniqueKey = UNIQUE_VIEW_KEY_PREFIX + articleId + ":" + ipAddress;
        Boolean first = redisTemplate.opsForValue().setIfAbsent(uniqueKey, "1", UNIQUE_TTL);
        if (Boolean.TRUE.equals(first)) {
            redisTemplate.opsForValue().increment(VIEW_KEY_PREFIX + articleId);
        }
    }
}