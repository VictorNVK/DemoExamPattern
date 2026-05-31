package ru.demoexam.template.util

import ru.demoexam.template.model.OrderFormModel
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object OrderValidation {
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    fun validate(form: OrderFormModel): List<String> {
        val errors = mutableListOf<String>()

        if (form.customerName.isBlank()) {
            errors += "Укажите ФИО клиента."
        }
        if (form.managerId == null) {
            errors += "Выберите менеджера."
        }
        if (form.status.isBlank()) {
            errors += "Выберите статус заказа."
        }
        if (form.pickupAddress.isBlank()) {
            errors += "Укажите адрес пункта выдачи."
        }
        if (parseDateTime(form.orderDateText) == null) {
            errors += "Дата заказа: формат дд.мм.гггг чч:мм"
        }
        if (form.productId == null) {
            errors += "Выберите товар."
        }
        if (form.quantityText.trim().toIntOrNull() == null || (form.quantityText.trim().toIntOrNull() ?: 0) <= 0) {
            errors += "Укажите корректное количество."
        }
        if (parseDecimal(form.unitPriceText) == null) {
            errors += "Укажите корректную цену."
        }
        if (form.discountText.isNotBlank() && parseDecimal(form.discountText) == null) {
            errors += "Скидка указана неверно."
        }

        return errors
    }

    fun parseDecimal(value: String): BigDecimal? {
        val normalized = value.trim().replace(',', '.')
        if (normalized.isBlank()) {
            return null
        }
        return runCatching { BigDecimal(normalized) }.getOrNull()
    }

    fun parseDateTime(value: String): LocalDateTime? {
        val text = value.trim()
        if (text.isBlank()) {
            return null
        }

        return runCatching {
            LocalDateTime.parse(text, dateTimeFormatter)
        }.getOrNull() ?: runCatching {
            LocalDate.parse(text, dateFormatter).atStartOfDay()
        }.getOrNull()
    }

    fun formatDateTime(value: LocalDateTime?): String {
        if (value == null) {
            return ""
        }
        return value.format(dateTimeFormatter)
    }
}
