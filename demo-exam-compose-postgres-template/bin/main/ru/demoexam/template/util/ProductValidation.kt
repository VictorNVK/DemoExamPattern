package ru.demoexam.template.util

import ru.demoexam.template.model.ProductFormModel
import java.math.BigDecimal

object ProductValidation {
    fun validate(form: ProductFormModel): List<String> {
        val errors = mutableListOf<String>()

        if (form.name.isBlank()) {
            errors += "Укажите наименование товара."
        }
        if (form.category.isBlank()) {
            errors += "Выберите категорию товара."
        }
        if (form.manufacturer.isBlank()) {
            errors += "Выберите производителя."
        }
        if (form.supplier.isBlank()) {
            errors += "Выберите поставщика."
        }
        if (form.unit.isBlank()) {
            errors += "Выберите единицу измерения."
        }

        val price = parseDecimal(form.priceText)
        if (price == null) {
            errors += "Цена должна быть числом."
        } else if (price < BigDecimal.ZERO) {
            errors += "Цена не может быть отрицательной."
        }

        val stock = form.stockQuantityText.trim().toIntOrNull()
        if (stock == null) {
            errors += "Количество на складе должно быть целым числом."
        } else if (stock < 0) {
            errors += "Количество на складе не может быть отрицательным."
        }

        val discount = parseDecimal(form.discountText)
        if (discount == null) {
            errors += "Скидка должна быть числом."
        } else {
            if (discount < BigDecimal.ZERO) {
                errors += "Скидка не может быть отрицательной."
            }
            if (discount > BigDecimal(100)) {
                errors += "Скидка не может превышать 100%."
            }
        }

        return errors
    }

    fun parseDecimal(value: String): BigDecimal? {
        val normalized = value.trim().replace(",", ".")
        if (normalized.isBlank()) {
            return null
        }
        return normalized.toBigDecimalOrNull()
    }
}
