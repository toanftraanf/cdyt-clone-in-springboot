package com.cdyt.be.dto.tag;

import lombok.Data;

@Data
public class TagResponseDto {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String color;
    private Integer displayOrder;
    private Long usageCount;

    // Computed fields
    private Boolean isActive;
    private Boolean isPopular; // Based on usage count
    private String displayNameWithCount;
    private Boolean hasCustomColor;
}