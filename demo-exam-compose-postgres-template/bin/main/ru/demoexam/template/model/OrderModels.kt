package ru.demoexam.template.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class OrderSummary(
    val id: Int,
    val customerName: String,
    val managerName: String,
    val statusName: String,
    val orderDate: LocalDateTime,
    val deliveryDate: LocalDateTime?,
    val pickupAddress: String,
    val pickupCode: String,
    val productArticle: String,
    val productName: String,
    val comment: String,
    val itemsCount: Int,
    val totalAmount: BigDecimal,
)

data class OrderDetail(
    val id: Int,
    val customerName: String,
    val managerId: Int,
    val managerName: String,
    val status: String,
    val pickupAddress: String,
    val orderDate: LocalDateTime,
    val deliveryDate: LocalDateTime?,
    val pickupCode: String,
    val comment: String,
    val productId: Int,
    val productArticle: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val discountPercent: BigDecimal,
    val totalAmount: BigDecimal,
)

data class OrderFormModel(
    val id: Int,
    val customerName: String = "",
    val managerId: Int? = null,
    val status: String = "",
    val pickupAddress: String = "",
    val orderDateText: String = "",
    val deliveryDateText: String = "",
    val pickupCode: String = "",
    val comment: String = "",
    val productId: Int? = null,
    val quantityText: String = "",
    val unitPriceText: String = "",
    val discountText: String = "",
)

data class LookupItem(
    val id: Int,
    val label: String,
)

data class OrderEditorPayload(
    val draft: OrderDetail,
    val managers: List<LookupItem>,
    val products: List<LookupItem>,
    val statuses: List<String>,
    val pickupAddresses: List<String>,
)

fun OrderDetail.toFormModel(): OrderFormModel = OrderFormModel(
    id = id,
    customerName = customerName,
    managerId = managerId,
    status = status,
    pickupAddress = pickupAddress,
    orderDateText = ru.demoexam.template.util.OrderValidation.formatDateTime(orderDate),
    deliveryDateText = ru.demoexam.template.util.OrderValidation.formatDateTime(deliveryDate),
    pickupCode = pickupCode,
    comment = comment,
    productId = productId,
    quantityText = quantity.toString(),
    unitPriceText = unitPrice.stripTrailingZeros().toPlainString(),
    discountText = discountPercent.stripTrailingZeros().toPlainString(),
)

fun OrderFormModel.toOrderDetail(): OrderDetail {
    val quantityValue = quantityText.trim().toIntOrNull() ?: 0
    val unitPriceValue = ru.demoexam.template.util.OrderValidation.parseDecimal(unitPriceText) ?: BigDecimal.ZERO
    val discountValue = ru.demoexam.template.util.OrderValidation.parseDecimal(discountText) ?: BigDecimal.ZERO
    val orderDateValue = ru.demoexam.template.util.OrderValidation.parseDateTime(orderDateText)
        ?: throw IllegalArgumentException("Некорректная дата заказа.")
    val deliveryDateValue = deliveryDateText.trim().takeIf { it.isNotBlank() }
        ?.let { ru.demoexam.template.util.OrderValidation.parseDateTime(it) }

    val lineTotal = unitPriceValue
        .multiply(BigDecimal(quantityValue))
        .multiply(BigDecimal.ONE.subtract(discountValue.divide(BigDecimal(100))))

    return OrderDetail(
        id = id,
        customerName = customerName.trim(),
        managerId = managerId ?: 0,
        managerName = "",
        status = status.trim(),
        pickupAddress = pickupAddress.trim(),
        orderDate = orderDateValue,
        deliveryDate = deliveryDateValue,
        pickupCode = pickupCode.trim(),
        comment = comment.trim(),
        productId = productId ?: 0,
        productArticle = "",
        productName = "",
        quantity = quantityValue,
        unitPrice = unitPriceValue,
        discountPercent = discountValue,
        totalAmount = lineTotal.setScale(2, java.math.RoundingMode.HALF_UP),
    )
}
