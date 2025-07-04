package com.cdyt.be.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

@Data
public class SearchRequestDto {

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 200, message = "Page size cannot exceed 200")
    private int pageSize = 50;

    @Min(value = 1, message = "Current page must be at least 1")
    private int currentPage = 1;

    private String textSearch = "";

    // Computed properties for Spring Data Pageable
    public int getOffset() {
        return (currentPage - 1) * pageSize;
    }

    public boolean hasTextSearch() {
        return textSearch != null && !textSearch.trim().isEmpty();
    }

    public String getCleanTextSearch() {
        return textSearch != null ? textSearch.trim() : "";
    }
}