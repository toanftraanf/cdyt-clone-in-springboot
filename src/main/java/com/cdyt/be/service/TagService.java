package com.cdyt.be.service;

import com.cdyt.be.common.exception.BusinessException;
import com.cdyt.be.dto.tag.CreateTagDto;
import com.cdyt.be.dto.tag.TagFilterDto;
import com.cdyt.be.dto.tag.TagResponseDto;
import com.cdyt.be.dto.tag.TagSearchRequestDto;
import com.cdyt.be.dto.tag.TagSearchResultDto;
import com.cdyt.be.dto.tag.UpdateTagDto;
import com.cdyt.be.entity.Tag;
import com.cdyt.be.mapper.TagMapper;
import com.cdyt.be.repository.TagRepository;
import com.cdyt.be.util.TextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    /**
     * Search tags with pagination and optional filters using request DTO
     * 
     * Filter behavior:
     * - name = null: Returns all tags (no name filtering)
     * - status = null: Returns ALL tags (both active and inactive)
     * - status = 1: Returns only active tags
     * - status = 0: Returns only inactive tags
     * - color = null: Returns all tags (no color filtering)
     * - Usage count filters: applied when provided
     */
    public Page<TagResponseDto> searchTags(TagSearchRequestDto searchRequest) {
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());
        Page<Tag> tags = tagRepository.findTagsWithFilters(
                searchRequest.getCleanName(),
                searchRequest.getStatus(),
                searchRequest.getCleanColor(),
                searchRequest.getMinUsageCount(),
                searchRequest.getMaxUsageCount(),
                pageable);

        List<TagResponseDto> tagDtos = tagMapper.toResponseDtoList(tags.getContent());
        return new PageImpl<>(tagDtos, pageable, tags.getTotalElements());
    }

    /**
     * Get all tags with pagination (legacy method)
     */
    public Page<TagResponseDto> getAllTags(Pageable pageable) {
        Page<Tag> tags = tagRepository.findAll(pageable);
        List<TagResponseDto> tagDtos = tagMapper.toResponseDtoList(tags.getContent());
        return new PageImpl<>(tagDtos, pageable, tags.getTotalElements());
    }

    /**
     * Get all active tags
     */
    public List<TagResponseDto> getAllActiveTags() {
        List<Tag> tags = tagRepository.findByStatusAndIsDeletedFalseOrderByDisplayOrderAscNameAsc(1);
        return tagMapper.toResponseDtoList(tags);
    }

    /**
     * Get all tags (including inactive but not deleted)
     */
    public List<TagResponseDto> getAllNonDeletedTags() {
        List<Tag> tags = tagRepository.findByIsDeletedFalseOrderByDisplayOrderAscNameAsc();
        return tagMapper.toResponseDtoList(tags);
    }

    /**
     * Get tag by ID
     */
    public Optional<TagResponseDto> getTagById(Long id) {
        return tagRepository.findById(id)
                .filter(tag -> !tag.getIsDeleted())
                .map(tagMapper::toResponseDto);
    }

    /**
     * Get tag by slug
     */
    public Optional<TagResponseDto> getTagBySlug(String slug) {
        return tagRepository.findBySlug(slug)
                .filter(tag -> !tag.getIsDeleted())
                .map(tagMapper::toResponseDto);
    }

    /**
     * Search tags by name
     */
    public List<TagResponseDto> searchTagsByName(String name) {
        List<Tag> tags = tagRepository
                .findByNameContainingIgnoreCaseAndIsDeletedFalseOrderByUsageCountDescNameAsc(name);
        return tagMapper.toResponseDtoList(tags);
    }

    /**
     * Get popular tags (usage count >= threshold)
     */
    public List<TagResponseDto> getPopularTags(Long threshold) {
        if (threshold == null || threshold < 0) {
            threshold = 10L; // Default threshold
        }
        List<Tag> tags = tagRepository
                .findByUsageCountGreaterThanEqualAndIsDeletedFalseOrderByUsageCountDescNameAsc(threshold);
        return tagMapper.toResponseDtoList(tags);
    }

    /**
     * Get top N most used ACTIVE tags (status = 1 and not deleted)
     */
    public List<TagResponseDto> getTopUsedTags(int limit) {
        Pageable pageable = PageRequest.of(0, Math.max(1, limit));
        List<Tag> tags = tagRepository.findTopUsedTags(pageable);
        return tagMapper.toResponseDtoList(tags);
    }

    /**
     * Get unused tags (usage count = 0)
     */
    public List<TagResponseDto> getUnusedTags() {
        List<Tag> tags = tagRepository.findByUsageCountAndIsDeletedFalseOrderByCreatedAtDesc(0L);
        return tagMapper.toResponseDtoList(tags);
    }

    /**
     * Get tags by color
     */
    public List<TagResponseDto> getTagsByColor(String color) {
        List<Tag> tags = tagRepository.findByColorAndIsDeletedFalseOrderByNameAsc(color);
        return tagMapper.toResponseDtoList(tags);
    }

    /**
     * Create a new tag
     */
    @Transactional
    public TagResponseDto createTag(CreateTagDto createDto) {
        Tag tag = tagMapper.toEntity(createDto);

        // Generate unique slug from name
        String baseSlug = TextUtils.generateSlug(createDto.getName());
        String uniqueSlug = generateUniqueSlug(baseSlug);
        tag.setSlug(uniqueSlug);

        Tag savedTag = tagRepository.save(tag);
        return tagMapper.toResponseDto(savedTag);
    }

    /**
     * Update an existing tag
     */
    @Transactional
    public TagResponseDto updateTag(Long id, UpdateTagDto updateDto) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Tag", id));

        if (tag.getIsDeleted()) {
            throw BusinessException.invalidState("Cannot update deleted tag");
        }

        // Handle slug generation/validation
        if (updateDto.getSlug() != null) {
            // User provided custom slug - validate uniqueness
            if (!updateDto.getSlug().equals(tag.getSlug()) &&
                    tagRepository.existsBySlugAndIdNot(updateDto.getSlug(), id)) {
                throw BusinessException.alreadyExists("Tag with slug", updateDto.getSlug());
            }
        } else if (updateDto.getName() != null && !updateDto.getName().equals(tag.getName())) {
            // Name changed but no custom slug provided - generate new slug
            String baseSlug = TextUtils.generateSlug(updateDto.getName());
            String uniqueSlug = generateUniqueSlug(baseSlug, id);
            updateDto.setSlug(uniqueSlug);
        }

        tagMapper.updateEntityFromDto(tag, updateDto);
        Tag savedTag = tagRepository.save(tag);
        return tagMapper.toResponseDto(savedTag);
    }

    /**
     * Delete a tag (soft delete)
     */
    @Transactional
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Tag", id));

        if (tag.getIsDeleted()) {
            throw BusinessException.invalidState("Tag is already deleted");
        }

        tag.setIsDeleted(true);
        tagRepository.save(tag);
    }

    /**
     * Restore a soft-deleted tag
     */
    @Transactional
    public TagResponseDto restoreTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Tag", id));

        if (!tag.getIsDeleted()) {
            throw BusinessException.invalidState("Tag is not deleted");
        }

        tag.setIsDeleted(false);
        Tag savedTag = tagRepository.save(tag);
        return tagMapper.toResponseDto(savedTag);
    }

    /**
     * Increment tag usage count
     */
    @Transactional
    public void incrementTagUsage(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Tag", id));

        if (!tag.isActive()) {
            throw BusinessException.invalidState("Cannot increment usage for inactive tag");
        }

        tag.incrementUsage();
        tagRepository.save(tag);
    }

    /**
     * Decrement tag usage count
     */
    @Transactional
    public void decrementTagUsage(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Tag", id));

        tag.decrementUsage();
        tagRepository.save(tag);
    }

    /**
     * Get tag statistics
     */
    public TagStats getTagStats() {
        Object[] stats = tagRepository.getTagStatistics();
        if (stats != null && stats.length >= 5) {
            return new TagStats(
                    ((Number) stats[0]).longValue(), // totalTags
                    ((Number) stats[1]).longValue(), // activeTags
                    ((Number) stats[2]).longValue(), // unusedTags
                    ((Number) stats[3]).longValue(), // maxUsageCount
                    ((Number) stats[4]).doubleValue() // avgUsageCount
            );
        }
        return new TagStats(0L, 0L, 0L, 0L, 0.0);
    }

    // Helper methods

    /**
     * Generate a unique slug by checking database and appending numbers if needed
     */
    private String generateUniqueSlug(String baseSlug) {
        return generateUniqueSlug(baseSlug, null);
    }

    /**
     * Generate a unique slug by checking database and appending numbers if needed
     * 
     * @param baseSlug  The base slug to make unique
     * @param excludeId ID to exclude from uniqueness check (for updates)
     */
    private String generateUniqueSlug(String baseSlug, Long excludeId) {
        if (baseSlug == null || baseSlug.isEmpty()) {
            baseSlug = "tag";
        }

        String slug = baseSlug;
        int counter = 1;

        // Check if slug exists (excluding the current tag for updates)
        while (excludeId != null
                ? tagRepository.existsBySlugAndIdNot(slug, excludeId)
                : tagRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }

        return slug;
    }

    /**
     * Tag statistics inner class
     */
    public static class TagStats {
        public final long totalTags;
        public final long activeTags;
        public final long unusedTags;
        public final long maxUsageCount;
        public final double avgUsageCount;

        public TagStats(long totalTags, long activeTags, long unusedTags, long maxUsageCount, double avgUsageCount) {
            this.totalTags = totalTags;
            this.activeTags = activeTags;
            this.unusedTags = unusedTags;
            this.maxUsageCount = maxUsageCount;
            this.avgUsageCount = avgUsageCount;
        }
    }
}