package ru.demoexam.backend.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record OrderSummaryDto(
        int id,
        String customerName,
        String managerName,
        String statusName,
        LocalDateTime orderDate,
        LocalDateTime deliveryDate,
        String pickupAddress,
        String pickupCode,
        String productArticle,
        String productName,
        String comment,
        int itemsCount,
        BigDecimal totalAmount
) {
}
