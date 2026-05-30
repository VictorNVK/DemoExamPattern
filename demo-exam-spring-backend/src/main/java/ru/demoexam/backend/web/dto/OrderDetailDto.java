package ru.demoexam.backend.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record OrderDetailDto(
        int id,
        String customerName,
        int managerId,
        String managerName,
        String status,
        String pickupAddress,
        LocalDateTime orderDate,
        LocalDateTime deliveryDate,
        String pickupCode,
        String comment,
        int productId,
        String productArticle,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal discountPercent,
        BigDecimal totalAmount
) {
}
