package ru.demoexam.backend.importer;

import java.math.BigDecimal;

public record TovarRow(
        String article,
        String name,
        String unit,
        BigDecimal price,
        String supplier,
        String manufacturer,
        String category,
        BigDecimal discountPercent,
        int stockQuantity,
        String description,
        String photoFileName
) {
}
