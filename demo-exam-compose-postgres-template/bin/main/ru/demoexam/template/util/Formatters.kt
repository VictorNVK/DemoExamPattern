package ru.demoexam.template.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

fun BigDecimal.toCurrencyText(): String {
    return "${setScale(2, RoundingMode.HALF_UP).toPlainString()} ₽"
}

fun BigDecimal.toPercentText(): String {
    return "${setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()}%"
}

fun LocalDateTime.toDisplayText(): String {
    return format(dateTimeFormatter)
}

