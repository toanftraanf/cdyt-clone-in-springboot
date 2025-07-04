package com.cdyt.be.mapper;

import com.cdyt.be.dto.tag.CreateTagDto;
import com.cdyt.be.dto.tag.TagResponseDto;
import com.cdyt.be.dto.tag.UpdateTagDto;
import com.cdyt.be.entity.Tag;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TagMapper {

    /**
     * Convert Tag entity to TagResponseDto
     */
    public TagResponseDto toResponseDto(Tag tag) {
        if (tag == null) {
            return null;
        }

        TagResponseDto dto = new TagResponseDto();
        dto.setId(tag.getId());
        dto.setName(tag.getName());
        dto.setSlug(tag.getSlug());
        dto.setDescription(tag.getDescription());
        dto.setColor(tag.getColor());
        dto.setDisplayOrder(tag.getDisplayOrder());
        dto.setUsageCount(tag.getUsageCount());

        // Computed fields
        dto.setIsActive(tag.isActive());
        dto.setIsPopular(tag.isPopular(10)); // Consider popular if used 10+ times
        dto.setDisplayNameWithCount(tag.getDisplayNameWithCount());
        dto.setHasCustomColor(tag.hasCustomColor());

        return dto;
    }

    /**
     * Convert CreateTagDto to Tag entity
     */
    public Tag toEntity(CreateTagDto createDto) {
        if (createDto == null) {
            return null;
        }

        Tag tag = new Tag();
        tag.setName(createDto.getName());
        tag.setDescription(createDto.getDescription());
        tag.setColor(createDto.getColor());
        tag.setDisplayOrder(createDto.getDisplayOrder());
        tag.setStatus(createDto.getStatus());
        tag.setIsDeleted(false);
        tag.setUsageCount(0L);

        return tag;
    }

    /**
     * Update Tag entity with UpdateTagDto data
     */
    public void updateEntityFromDto(Tag tag, UpdateTagDto updateDto) {
        if (updateDto == null || tag == null) {
            return;
        }

        if (updateDto.getName() != null) {
            tag.setName(updateDto.getName());
        }

        if (updateDto.getSlug() != null) {
            tag.setSlug(updateDto.getSlug());
        }

        if (updateDto.getDescription() != null) {
            tag.setDescription(updateDto.getDescription());
        }

        if (updateDto.getColor() != null) {
            tag.setColor(updateDto.getColor());
        }

        if (updateDto.getDisplayOrder() != null) {
            tag.setDisplayOrder(updateDto.getDisplayOrder());
        }

        if (updateDto.getStatus() != null) {
            tag.setStatus(updateDto.getStatus());
        }
    }

    /**
     * Convert list of Tag entities to list of TagResponseDto
     */
    public List<TagResponseDto> toResponseDtoList(List<Tag> tags) {
        return tags.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }
}