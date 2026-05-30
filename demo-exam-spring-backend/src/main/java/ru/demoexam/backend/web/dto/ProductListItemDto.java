package ru.demoexam.backend.web.dto;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record ProductListItemDto(
        int id,
        String article,
        String name,
        String categoryName,
        String description,
        String manufacturerName,
        String supplierName,
        String unitName,
        BigDecimal price,
        int stockQuantity,
        BigDecimal discountPercent,
        String imagePath
) {
}
