package com.cdyt.be.controller;

import com.cdyt.be.common.annotation.RequireAuth;
import com.cdyt.be.common.controller.BaseAuthController;
import com.cdyt.be.common.dto.ApiResponse;
import com.cdyt.be.common.util.ResponseUtils;
import com.cdyt.be.dto.tag.CreateTagDto;
import com.cdyt.be.dto.tag.TagResponseDto;
import com.cdyt.be.dto.tag.TagSearchRequestDto;
import com.cdyt.be.dto.tag.UpdateTagDto;
import com.cdyt.be.service.TagService;
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
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@RequireAuth(checkPermissions = true)
@Tag(name = "Tag Management", description = "APIs for managing tags")
public class TagController extends BaseAuthController {

        private final TagService tagService;

        @PostMapping("/getAll")
        @Operation(summary = "Search tags with pagination and filters", description = "Search tags using POST with pagination and optional filters in request body")
        public ResponseEntity<ApiResponse<Page<TagResponseDto>>> searchTags(
                        @Valid @RequestBody TagSearchRequestDto searchRequest) {
                Page<TagResponseDto> tags = tagService.searchTags(searchRequest);
                return ok(tags, "Tags search completed successfully",
                                Map.of("totalElements", tags.getTotalElements(),
                                                "totalPages", tags.getTotalPages(),
                                                "currentPage", tags.getNumber(),
                                                "pageSize", tags.getSize(),
                                                "hasFilters", searchRequest.hasFilters()));
        }

        // Commented out endpoints - keeping only essential ones
        /*
         * @GetMapping("/all")
         * 
         * @Operation(summary = "Get all non-deleted tags", description =
         * "Retrieves all tags including inactive but not deleted")
         * public ResponseEntity<ApiResponse<List<TagResponseDto>>>
         * getAllNonDeletedTags() {
         * List<TagResponseDto> tags = tagService.getAllNonDeletedTags();
         * return ok(tags, "All tags retrieved successfully",
         * ResponseUtils.listMetadata(tags.size()));
         * }
         * 
         * @GetMapping("/popular")
         * 
         * @Operation(summary = "Get popular tags", description =
         * "Retrieves tags with high usage count")
         * public ResponseEntity<ApiResponse<List<TagResponseDto>>> getPopularTags(
         * 
         * @Parameter(description = "Usage count threshold") @RequestParam(defaultValue
         * = "10") Long threshold) {
         * List<TagResponseDto> tags = tagService.getPopularTags(threshold);
         * return ok(tags, "Popular tags retrieved successfully",
         * Map.of("threshold", threshold, "count", tags.size()));
         * }
         * 
         * @GetMapping("/unused")
         * 
         * @Operation(summary = "Get unused tags", description =
         * "Retrieves tags that are not being used")
         * public ResponseEntity<ApiResponse<List<TagResponseDto>>> getUnusedTags() {
         * List<TagResponseDto> tags = tagService.getUnusedTags();
         * return ok(tags, "Unused tags retrieved successfully",
         * ResponseUtils.listMetadata(tags.size()));
         * }
         * 
         * @GetMapping("/by-color")
         * 
         * @Operation(summary = "Get tags by color", description =
         * "Retrieves tags with specific color")
         * public ResponseEntity<ApiResponse<List<TagResponseDto>>> getTagsByColor(
         * 
         * @Parameter(description = "Hex color code") @RequestParam String color) {
         * List<TagResponseDto> tags = tagService.getTagsByColor(color);
         * return ok(tags, "Tags by color retrieved successfully",
         * Map.of("color", color, "count", tags.size()));
         * }
         * 
         * @GetMapping("/{id}")
         * 
         * @Operation(summary = "Get tag by ID", description =
         * "Retrieves a tag by its unique identifier")
         * public ResponseEntity<ApiResponse<TagResponseDto>> getTagById(
         * 
         * @Parameter(description = "Tag ID") @PathVariable Long id) {
         * return tagService.getTagById(id)
         * .map(tag -> ok(tag, "Tag found successfully",
         * ResponseUtils.operationMetadata("getTagById", id)))
         * .orElse(notFound("Tag not found with ID: " + id));
         * }
         * 
         * @GetMapping("/slug/{slug}")
         * 
         * @Operation(summary = "Get tag by slug", description =
         * "Retrieves a tag by its slug")
         * public ResponseEntity<ApiResponse<TagResponseDto>> getTagBySlug(
         * 
         * @Parameter(description = "Tag slug") @PathVariable String slug) {
         * return tagService.getTagBySlug(slug)
         * .map(tag -> ok(tag, "Tag found successfully",
         * ResponseUtils.operationMetadata("getTagBySlug", slug)))
         * .orElse(notFound("Tag not found with slug: " + slug));
         * }
         * 
         * @GetMapping("/search")
         * 
         * @Operation(summary = "Search tags", description = "Search tags by name")
         * public ResponseEntity<ApiResponse<List<TagResponseDto>>> searchTags(
         * 
         * @Parameter(description = "Search term") @RequestParam String name) {
         * List<TagResponseDto> tags = tagService.searchTagsByName(name);
         * return ok(tags, "Tag search completed",
         * Map.of("searchTerm", name, "resultsCount", tags.size()));
         * }
         */

        @PostMapping
        @Operation(summary = "Create a new tag", description = "Creates a new tag with automatic slug generation")
        public ResponseEntity<ApiResponse<TagResponseDto>> createTag(
                        @Valid @RequestBody CreateTagDto createDto) {
                TagResponseDto createdTag = tagService.createTag(createDto);
                return created(createdTag, "Tag created successfully",
                                ResponseUtils.operationMetadata("createTag", createdTag.getSlug()));
        }

        @PutMapping("/{id}")
        @Operation(summary = "Update tag", description = "Updates an existing tag's information")
        public ResponseEntity<ApiResponse<TagResponseDto>> updateTag(
                        @Parameter(description = "Tag ID") @PathVariable Long id,
                        @Valid @RequestBody UpdateTagDto updateDto) {
                try {
                        TagResponseDto updatedTag = tagService.updateTag(id, updateDto);
                        return ok(updatedTag, "Tag updated successfully",
                                        ResponseUtils.operationMetadata("updateTag", id));
                } catch (RuntimeException e) {
                        return notFound("Tag not found with ID: " + id);
                }
        }

        @DeleteMapping("/{id}")
        @Operation(summary = "Delete tag", description = "Soft deletes a tag (marks as deleted)")
        public ResponseEntity<ApiResponse<Void>> deleteTag(
                        @Parameter(description = "Tag ID") @PathVariable Long id) {
                try {
                        tagService.deleteTag(id);
                        return ok(null, "Tag deleted successfully",
                                        ResponseUtils.operationMetadata("deleteTag", id));
                } catch (RuntimeException e) {
                        return notFound("Tag not found with ID: " + id);
                }
        }

        @PostMapping("/{id}/restore")
        @Operation(summary = "Restore tag", description = "Restores a soft-deleted tag")
        public ResponseEntity<ApiResponse<TagResponseDto>> restoreTag(
                        @Parameter(description = "Tag ID") @PathVariable Long id) {
                try {
                        TagResponseDto restoredTag = tagService.restoreTag(id);
                        return ok(restoredTag, "Tag restored successfully",
                                        ResponseUtils.operationMetadata("restoreTag", id));
                } catch (RuntimeException e) {
                        return notFound("Tag not found with ID: " + id);
                }
        }

        @PostMapping("/{id}/increment-usage")
        @Operation(summary = "Increment tag usage", description = "Increments the usage count of a tag")
        public ResponseEntity<ApiResponse<Void>> incrementTagUsage(
                        @Parameter(description = "Tag ID") @PathVariable Long id) {
                try {
                        tagService.incrementTagUsage(id);
                        return ok(null, "Tag usage incremented successfully",
                                        ResponseUtils.operationMetadata("incrementTagUsage", id));
                } catch (RuntimeException e) {
                        return badRequest("Error incrementing tag usage: " + e.getMessage());
                }
        }

        @PostMapping("/{id}/decrement-usage")
        @Operation(summary = "Decrement tag usage", description = "Decrements the usage count of a tag")
        public ResponseEntity<ApiResponse<Void>> decrementTagUsage(
                        @Parameter(description = "Tag ID") @PathVariable Long id) {
                try {
                        tagService.decrementTagUsage(id);
                        return ok(null, "Tag usage decremented successfully",
                                        ResponseUtils.operationMetadata("decrementTagUsage", id));
                } catch (RuntimeException e) {
                        return badRequest("Error decrementing tag usage: " + e.getMessage());
                }
        }

        @GetMapping("/stats")
        @Operation(summary = "Get tag statistics", description = "Retrieves tag usage statistics")
        public ResponseEntity<ApiResponse<TagService.TagStats>> getTagStats() {
                TagService.TagStats stats = tagService.getTagStats();
                return ok(stats, "Tag statistics retrieved successfully",
                                ResponseUtils.operationMetadata("getTagStats", null));
        }

}