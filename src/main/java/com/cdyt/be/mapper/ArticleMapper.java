package com.cdyt.be.mapper;

import com.cdyt.be.dto.article.ArticleDetailResponseDto;
import com.cdyt.be.dto.article.ArticleResponseDto;
import com.cdyt.be.dto.article.CreateArticleDto;
import com.cdyt.be.entity.Article;
import com.cdyt.be.entity.Category;
import com.cdyt.be.entity.Tag;
import com.cdyt.be.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ArticleMapper {

    /**
     * Convert Article entity to ArticleResponseDto (optimized for list operations)
     */
    public ArticleResponseDto toResponseDto(Article article) {
        if (article == null) {
            return null;
        }

        ArticleResponseDto dto = new ArticleResponseDto();
        dto.setId(article.getId());
        dto.setTitle(article.getTitle());
        dto.setSlug(article.getSlug());
        dto.setSummary(article.getSummary());
        dto.setAuthorId(article.getAuthor() != null ? article.getAuthor().getId() : null);
        dto.setAuthorName(article.getAuthor() != null ? article.getAuthor().getFullName() : null);
        dto.setCategoryId(article.getCategory() != null ? article.getCategory().getId() : null);
        dto.setCategoryName(article.getCategory() != null ? article.getCategory().getName() : null);

        // Map tags for list view (basic info only)
        if (article.getTags() != null && !article.getTags().isEmpty()) {
            List<ArticleResponseDto.TagInfo> tagInfos = article.getTags().stream()
                    .map(tag -> {
                        ArticleResponseDto.TagInfo tagInfo = new ArticleResponseDto.TagInfo();
                        tagInfo.setId(tag.getId());
                        tagInfo.setName(tag.getName());
                        tagInfo.setColor(tag.getColor());
                        return tagInfo;
                    })
                    .collect(Collectors.toList());
            dto.setTags(tagInfos);
        }

        dto.setCoverImageUrl(article.getCoverImageUrl());
        dto.setStatus(article.getStatus());
        dto.setPublishedAt(article.getPublishedAt());
        dto.setViewCount(article.getViewCount());
        dto.setLikeCount(article.getLikeCount());
        dto.setCommentCount(article.getCommentCount());
        dto.setCreatedAt(article.getCreatedAt());
        dto.setUpdatedAt(article.getUpdatedAt());

        return dto;
    }

    /**
     * Convert Article entity to ArticleDetailResponseDto (detailed view with full
     * related entities)
     */
    public ArticleDetailResponseDto toDetailResponseDto(Article article) {
        if (article == null) {
            return null;
        }

        ArticleDetailResponseDto dto = new ArticleDetailResponseDto();
        dto.setId(article.getId());
        dto.setTitle(article.getTitle());
        dto.setSlug(article.getSlug());
        dto.setSummary(article.getSummary());
        dto.setContent(article.getContent());

        // Populate nested author information
        if (article.getAuthor() != null) {
            ArticleDetailResponseDto.AuthorInfo authorInfo = new ArticleDetailResponseDto.AuthorInfo();
            authorInfo.setId(article.getAuthor().getId());
            authorInfo.setName(article.getAuthor().getFullName());
            authorInfo.setAvatar(article.getAuthor().getAvatar());
            authorInfo.setEmail(article.getAuthor().getEmail());
            dto.setAuthor(authorInfo);
        }

        // Populate nested category information
        if (article.getCategory() != null) {
            ArticleDetailResponseDto.CategoryInfo categoryInfo = new ArticleDetailResponseDto.CategoryInfo();
            categoryInfo.setId(article.getCategory().getId());
            categoryInfo.setName(article.getCategory().getName());
            categoryInfo.setDescription(article.getCategory().getDescription());
            dto.setCategory(categoryInfo);
        }

        // Map tags for detailed view (full info)
        if (article.getTags() != null && !article.getTags().isEmpty()) {
            List<ArticleDetailResponseDto.TagInfo> tagInfos = article.getTags().stream()
                    .map(tag -> {
                        ArticleDetailResponseDto.TagInfo tagInfo = new ArticleDetailResponseDto.TagInfo();
                        tagInfo.setId(tag.getId());
                        tagInfo.setName(tag.getName());
                        tagInfo.setSlug(tag.getSlug());
                        tagInfo.setDescription(tag.getDescription());
                        tagInfo.setColor(tag.getColor());
                        tagInfo.setUsageCount(tag.getUsageCount());
                        return tagInfo;
                    })
                    .collect(Collectors.toList());
            dto.setTags(tagInfos);
        }

        dto.setCoverImageUrl(article.getCoverImageUrl());
        dto.setStatus(article.getStatus());
        dto.setPublishedAt(article.getPublishedAt());
        dto.setViewCount(article.getViewCount());
        dto.setLikeCount(article.getLikeCount());
        dto.setCommentCount(article.getCommentCount());
        dto.setSeoTitle(article.getSeoTitle());
        dto.setSeoDescription(article.getSeoDescription());
        dto.setSeoKeywords(article.getSeoKeywords());
        dto.setIsDeleted(article.getIsDeleted());
        dto.setCreatedAt(article.getCreatedAt());
        dto.setUpdatedAt(article.getUpdatedAt());

        return dto;
    }

    /**
     * Convert CreateArticleDto to Article entity
     */
    public Article toEntity(CreateArticleDto createDto) {
        if (createDto == null) {
            return null;
        }

        Article article = new Article();
        article.setTitle(createDto.getTitle());
        article.setSummary(createDto.getSummary());
        article.setContent(createDto.getContent());

        // Set category if provided
        if (createDto.getCategoryId() != null) {
            Category category = new Category();
            category.setId(createDto.getCategoryId());
            article.setCategory(category);
        }

        // Note: Tags are handled separately in the service layer with proper validation
        // and automatic usage tracking, so we don't set them here

        article.setCoverImageUrl(createDto.getCoverImageUrl());
        article.setStatus(createDto.getStatus() != null ? createDto.getStatus() : Article.STATUS_DRAFT);
        article.setSeoTitle(createDto.getSeoTitle());
        article.setSeoDescription(createDto.getSeoDescription());
        article.setSeoKeywords(createDto.getSeoKeywords());

        // Default values for new articles
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCommentCount(0);
        article.setIsDeleted(false);

        return article;
    }

    /**
     * Convert list of Article entities to list of ArticleResponseDto (for list
     * operations)
     */
    public List<ArticleResponseDto> toResponseDtoList(List<Article> articles) {
        if (articles == null) {
            return null;
        }

        return articles.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of Article entities to list of ArticleDetailResponseDto (for
     * detailed views)
     */
    public List<ArticleDetailResponseDto> toDetailResponseDtoList(List<Article> articles) {
        if (articles == null) {
            return null;
        }

        return articles.stream()
                .map(this::toDetailResponseDto)
                .collect(Collectors.toList());
    }
}