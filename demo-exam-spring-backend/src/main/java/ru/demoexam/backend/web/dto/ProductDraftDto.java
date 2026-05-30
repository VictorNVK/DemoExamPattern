package ru.demoexam.backend.web.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ProductDraftDto(
        int id,
        String name,
        String category,
        String description,
        String manufacturer,
        String supplier,
        String unit,
        BigDecimal price,
        Integer stockQuantity,
        BigDecimal discountPercent,
        String imagePath
) {
}
