package com.cdyt.be.dto.category;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCategoryDto {

    @Size(max = 255, message = "Name must not exceed 255 characters")
    private String name;

    @Size(max = 255, message = "Slug must not exceed 255 characters")
    private String slug;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private Long parentId;

    private Integer displayOrder;

    private Integer status;
}