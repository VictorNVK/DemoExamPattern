package ru.demoexam.backend.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record OrderDraftDto(
        int id,
        String customerName,
        Integer managerId,
        String status,
        String pickupAddress,
        LocalDateTime orderDate,
        LocalDateTime deliveryDate,
        String pickupCode,
        String comment,
        Integer productId,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal discountPercent
) {
}
