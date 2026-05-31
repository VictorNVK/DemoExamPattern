package ru.demoexam.backend.importer;

import java.time.LocalDateTime;

public record OrderImportRow(
        String customerName,
        String status,
        String pickupAddress,
        LocalDateTime orderDate,
        LocalDateTime deliveryDate,
        String pickupCode,
        String article,
        int quantity
) {
}
