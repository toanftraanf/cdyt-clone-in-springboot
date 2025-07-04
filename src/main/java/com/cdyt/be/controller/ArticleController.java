package com.cdyt.be.controller;

import com.cdyt.be.common.annotation.RequireAuth;
import com.cdyt.be.common.controller.BaseAuthController;
import com.cdyt.be.common.dto.ApiResponse;
import com.cdyt.be.common.util.ResponseUtils;
import com.cdyt.be.dto.article.ArticleDetailResponseDto;
import com.cdyt.be.dto.article.ArticleResponseDto;
import com.cdyt.be.dto.article.ArticleSearchRequestDto;
import com.cdyt.be.dto.article.CreateArticleDto;
import com.cdyt.be.dto.article.UpdateArticleDto;
import com.cdyt.be.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@RequireAuth
@Tag(name = "Article Management", description = "APIs for managing articles")
public class ArticleController extends BaseAuthController {

  private final ArticleService articleService;

  @PostMapping("/getAll")
  @Operation(summary = "Search articles with pagination and filters", description = "Search articles using POST with pagination and optional filters in request body")
  public ResponseEntity<ApiResponse<Page<ArticleResponseDto>>> searchArticles(
      @Valid @RequestBody ArticleSearchRequestDto searchRequest) {
    Page<ArticleResponseDto> articles = articleService.searchArticles(searchRequest);
    return ok(articles, "Articles search completed successfully",
        Map.of("totalElements", articles.getTotalElements(),
            "totalPages", articles.getTotalPages(),
            "currentPage", articles.getNumber(),
            "pageSize", articles.getSize(),
            "hasFilters", searchRequest.hasFilters()));
  }

  @PostMapping("/create")
  @Operation(summary = "Create a new article", description = "Creates a new article with automatic slug generation and author assignment")
  public ResponseEntity<ApiResponse<ArticleResponseDto>> createArticle(
      @Valid @RequestBody CreateArticleDto createDto) {
    ArticleResponseDto createdArticle = articleService.createArticle(createDto);
    return created(createdArticle, "Article created successfully",
        ResponseUtils.operationMetadata("createArticle", createdArticle.getSlug()));
  }

  @GetMapping("/stats")
  @Operation(summary = "Get article statistics", description = "Retrieves article statistics by status")
  public ResponseEntity<ApiResponse<ArticleService.ArticleStats>> getArticleStats() {
    ArticleService.ArticleStats stats = articleService.getArticleStats();
    return ok(stats, "Article statistics retrieved successfully", null);
  }

  @GetMapping("/published")
  @Operation(summary = "Get all published articles", description = "Retrieves all published articles")
  public ResponseEntity<ApiResponse<List<ArticleResponseDto>>> getAllPublishedArticles() {
    List<ArticleResponseDto> articles = articleService.getAllPublishedArticles();
    return ok(articles, "Published articles retrieved successfully",
        ResponseUtils.listMetadata(articles.size()));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get article by ID", description = "Retrieves an article by its unique identifier with full details")
  public ResponseEntity<ApiResponse<ArticleDetailResponseDto>> getArticleById(
      @Parameter(description = "Article ID") @PathVariable Long id) {
    return articleService.getArticleById(id)
        .map(article -> ok(article, "Article found successfully",
            ResponseUtils.operationMetadata("getArticleById", id)))
        .orElse(notFound("Article not found with ID: " + id));
  }

  @GetMapping("/slug/{slug}")
  @Operation(summary = "Get article by slug", description = "Retrieves an article by its slug with full details")
  public ResponseEntity<ApiResponse<ArticleDetailResponseDto>> getArticleBySlug(
      @Parameter(description = "Article slug") @PathVariable String slug) {
    return articleService.getArticleBySlug(slug)
        .map(article -> ok(article, "Article found successfully",
            ResponseUtils.operationMetadata("getArticleBySlug", slug)))
        .orElse(notFound("Article not found with slug: " + slug));
  }

  @GetMapping("/author/{authorId}")
  @Operation(summary = "Get articles by author", description = "Retrieves articles by author ID")
  public ResponseEntity<ApiResponse<List<ArticleResponseDto>>> getArticlesByAuthor(
      @Parameter(description = "Author ID") @PathVariable Long authorId) {
    List<ArticleResponseDto> articles = articleService.getArticlesByAuthor(authorId);
    return ok(articles, "Articles by author retrieved successfully",
        Map.of("authorId", authorId, "count", articles.size()));
  }

  @GetMapping("/category/{categoryId}")
  @Operation(summary = "Get articles by category", description = "Retrieves articles from the specified category and all its child categories (hierarchical search)")
  public ResponseEntity<ApiResponse<List<ArticleResponseDto>>> getArticlesByCategory(
      @Parameter(description = "Category ID") @PathVariable Long categoryId) {
    List<ArticleResponseDto> articles = articleService.getArticlesByCategory(categoryId);
    return ok(articles, "Articles by category retrieved successfully",
        Map.of("categoryId", categoryId, "count", articles.size(), "includesChildCategories", true));
  }

  @GetMapping("/popular")
  @Operation(summary = "Get popular articles", description = "Retrieves articles with high view count")
  public ResponseEntity<ApiResponse<List<ArticleResponseDto>>> getPopularArticles(
      @Parameter(description = "View count threshold") @RequestParam(defaultValue = "100") Integer threshold) {
    List<ArticleResponseDto> articles = articleService.getPopularArticles(threshold);
    return ok(articles, "Popular articles retrieved successfully",
        Map.of("threshold", threshold, "count", articles.size()));
  }

  @PostMapping("/update")
  @Operation(summary = "Update article", description = "Updates an existing article with automatic tag usage tracking. Only provided fields will be updated.")
  public ResponseEntity<ApiResponse<ArticleResponseDto>> updateArticle(
      @Valid @RequestBody UpdateArticleDto updateDto) {
    try {
      ArticleResponseDto updatedArticle = articleService.updateArticle(updateDto.getId(), updateDto);
      return ok(updatedArticle, "Article updated successfully",
          ResponseUtils.operationMetadata("updateArticle", updateDto.getId()));
    } catch (RuntimeException e) {
      if (e.getMessage().contains("not found")) {
        return notFound(e.getMessage());
      } else if (e.getMessage().contains("deleted")) {
        return badRequest("Cannot update deleted article");
      } else if (e.getMessage().contains("No updates provided")) {
        return badRequest("No updates provided in request body");
      } else if (e.getMessage().contains("Tag not found")) {
        return badRequest(e.getMessage());
      } else {
        return badRequest("Update failed: " + e.getMessage());
      }
    }
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete article", description = "Soft deletes an article with automatic tag usage tracking")
  public ResponseEntity<ApiResponse<Void>> deleteArticle(
      @Parameter(description = "Article ID") @PathVariable Long id) {
    try {
      articleService.deleteArticle(id);
      return ok(null, "Article deleted successfully",
          ResponseUtils.operationMetadata("deleteArticle", id));
    } catch (RuntimeException e) {
      return notFound("Article not found with ID: " + id);
    }
  }

  @PostMapping("/{id}/restore")
  @Operation(summary = "Restore article", description = "Restores a soft-deleted article with automatic tag usage tracking")
  public ResponseEntity<ApiResponse<ArticleResponseDto>> restoreArticle(
      @Parameter(description = "Article ID") @PathVariable Long id) {
    try {
      ArticleResponseDto restoredArticle = articleService.restoreArticle(id);
      return ok(restoredArticle, "Article restored successfully",
          ResponseUtils.operationMetadata("restoreArticle", id));
    } catch (RuntimeException e) {
      return notFound("Article not found with ID: " + id);
    }
  }
}