package com.cdyt.be.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

/**
 * Base class chứa thuộc tính phân trang dùng chung cho *SearchRequestDto
 */
@Getter
@Setter
public abstract class BasePaginationRequestDto {

    @Min(value = 0, message = "Page number must be 0 or greater")
    private int page = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    private int size = 10;
}