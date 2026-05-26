package ru.demoexam.template.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class OrderSummary(
    val id: Int,
    val customerName: String,
    val managerName: String,
    val statusName: String,
    val orderDate: LocalDateTime,
    val comment: String,
    val itemsCount: Int,
    val totalAmount: BigDecimal,
)

