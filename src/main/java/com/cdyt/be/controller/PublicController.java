package com.cdyt.be.controller;

import com.cdyt.be.common.controller.BaseAuthController;
import com.cdyt.be.common.dto.ApiResponse;
import com.cdyt.be.common.util.ResponseUtils;
import com.cdyt.be.dto.article.ArticleDetailResponseDto;
import com.cdyt.be.dto.article.ArticleResponseDto;
import com.cdyt.be.dto.article.ArticlesByTagSearchRequestDto;
import com.cdyt.be.dto.article.PublicArticleSearchRequestDto;
import com.cdyt.be.dto.category.CategoryResponseDto;
import com.cdyt.be.dto.tag.TagResponseDto;
import com.cdyt.be.service.ArticleService;
import com.cdyt.be.service.CategoryService;
import com.cdyt.be.service.TagService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public")
@Tag(name = "Public", description = "Public APIs for guest users")
public class PublicController extends BaseAuthController {

  private final ArticleService articleService;
  private final TagService tagService;
  private final CategoryService categoryService;

  @GetMapping("/getAllCategories")
  @Operation(summary = "Get category hierarchy", description = "Retrieves all categories in hierarchical structure with parent-child relationships")
  public ResponseEntity<ApiResponse<List<CategoryResponseDto>>> getCategoryHierarchy() {
    List<CategoryResponseDto> hierarchy = categoryService.getCategoryHierarchy();
    return ok(hierarchy, "Category hierarchy retrieved successfully",
        Map.of("totalRootCategories", hierarchy.size(),
            "structureType", "hierarchical"));
  }

  @GetMapping("/getAllTags")
  @Operation(summary = "Get all active tags", description = "Retrieves all active tags")
  public ResponseEntity<ApiResponse<List<TagResponseDto>>> getAllActiveTags() {
    List<TagResponseDto> tags = tagService.getAllActiveTags();
    return ok(tags, "Active tags retrieved successfully",
        ResponseUtils.listMetadata(tags.size()));
  }

  @GetMapping("/getAllTopUsedTags")
  @Operation(summary = "Get top used active tags", description = "Retrieves the most used active tags (status = 1)")
  public ResponseEntity<ApiResponse<List<TagResponseDto>>> getTopUsedTags(
      @Parameter(description = "Number of tags to return") @RequestParam(defaultValue = "20") int limit) {
    List<TagResponseDto> tags = tagService.getTopUsedTags(limit);
    return ok(tags, "Top used active tags retrieved successfully",
        Map.of("limit", limit, "count", tags.size()));
  }

  // ========== ARTICLE ENDPOINTS ==========

  @PostMapping("/articles/getAll")
  @Operation(summary = "Search published articles", description = "Search published articles with pagination, title search, and date range filtering using POST with request body")
  public ResponseEntity<ApiResponse<Page<ArticleResponseDto>>> getAllPublishedArticles(
      @Valid @RequestBody PublicArticleSearchRequestDto searchRequest) {
    Page<ArticleResponseDto> articles = articleService.getPublishedArticlesWithFilters(
        searchRequest);
    return ok(articles, "Published articles search completed successfully",
        Map.of("totalElements", articles.getTotalElements(),
            "totalPages", articles.getTotalPages(),
            "currentPage", articles.getNumber(),
            "pageSize", articles.getSize(),
            "hasFilters", searchRequest.hasFilters()));
  }

  @GetMapping("/articles/{slug}")
  @Operation(summary = "Get published article by slug", description = "Retrieves a published article by its slug with full details")
  public ResponseEntity<ApiResponse<ArticleDetailResponseDto>> getPublishedArticleBySlug(
      @Parameter(description = "Article slug") @PathVariable String slug) {
    return articleService.getPublishedArticleBySlug(slug)
        .map(article -> {
          // Ghi nhận view
          String ip = getClientIpAddress();
          articleService.recordView(article.getId(), ip);
          return ok(article, "Article found successfully",
              ResponseUtils.operationMetadata("getPublishedArticleBySlug", slug));
        })
        .orElse(notFound("Published article not found with slug: " + slug));
  }

  @GetMapping("/articles/id/{id}")
  @Operation(summary = "Get published article by ID", description = "Retrieves a published article by its ID with full details")
  public ResponseEntity<ApiResponse<ArticleDetailResponseDto>> getPublishedArticleById(
      @Parameter(description = "Article ID") @PathVariable Long id) {
    return articleService.getPublishedArticleById(id)
        .map(article -> {
          String ip = getClientIpAddress();
          articleService.recordView(article.getId(), ip);
          return ok(article, "Article found successfully",
              ResponseUtils.operationMetadata("getPublishedArticleById", id));
        })
        .orElse(notFound("Published article not found with ID: " + id));
  }

  @GetMapping("/articles/category/{categoryId}")
  @Operation(summary = "Get published articles by category", description = "Retrieves published articles from the specified category and all its child categories")
  public ResponseEntity<ApiResponse<List<ArticleResponseDto>>> getPublishedArticlesByCategory(
      @Parameter(description = "Category ID") @PathVariable Long categoryId) {
    List<ArticleResponseDto> articles = articleService.getPublishedArticlesByCategory(categoryId);
    return ok(articles, "Published articles by category retrieved successfully",
        Map.of("categoryId", categoryId, "count", articles.size(), "includesChildCategories", true));
  }

  @GetMapping("/articles/popular")
  @Operation(summary = "Get popular published articles", description = "Retrieves published articles with high view count")
  public ResponseEntity<ApiResponse<List<ArticleResponseDto>>> getPopularPublishedArticles(
      @Parameter(description = "View count threshold") @RequestParam(defaultValue = "100") Integer threshold) {
    List<ArticleResponseDto> articles = articleService.getPopularPublishedArticles(threshold);
    return ok(articles, "Popular published articles retrieved successfully",
        Map.of("threshold", threshold, "count", articles.size()));
  }

  @GetMapping("/articles/search")
  @Operation(summary = "Search published articles", description = "Search published articles by title")
  public ResponseEntity<ApiResponse<List<ArticleResponseDto>>> searchPublishedArticles(
      @Parameter(description = "Search query") @RequestParam String query) {
    List<ArticleResponseDto> articles = articleService.searchPublishedArticlesByTitle(query);
    return ok(articles, "Published articles search completed successfully",
        Map.of("query", query, "count", articles.size()));
  }

  @GetMapping("/articles/stats")
  @Operation(summary = "Get published article statistics", description = "Retrieves statistics for published articles only")
  public ResponseEntity<ApiResponse<Map<String, Object>>> getPublishedArticleStats() {
    Map<String, Object> stats = articleService.getPublishedArticleStats();
    return ok(stats, "Published article statistics retrieved successfully", null);
  }

  @PostMapping("/articles/tag")
  @Operation(summary = "Get published articles by tag", description = "Search published articles by tag with pagination, title search, and published date range filtering")
  public ResponseEntity<ApiResponse<Page<ArticleResponseDto>>> getPublishedArticlesByTag(
      @Valid @RequestBody ArticlesByTagSearchRequestDto searchRequest) {
    Page<ArticleResponseDto> articles = articleService.getPublishedArticlesByTag(searchRequest);
    return ok(articles, "Published articles by tag retrieved successfully",
        Map.of("tagId", searchRequest.getTagId(),
            "totalElements", articles.getTotalElements(),
            "totalPages", articles.getTotalPages(),
            "currentPage", articles.getNumber(),
            "pageSize", articles.getSize(),
            "hasFilters", searchRequest.hasFilters()));
  }
}
