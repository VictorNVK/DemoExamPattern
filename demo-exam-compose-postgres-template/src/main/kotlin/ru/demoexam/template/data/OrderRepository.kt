package ru.demoexam.template.data

import kotlinx.coroutines.runBlocking
import ru.demoexam.template.data.local.OrderWithDetails
import ru.demoexam.template.model.OrderSummary
import java.math.BigDecimal

class OrderRepository {
    fun findAll(): List<OrderSummary> {
        return runBlocking {
            AppDatabaseProvider.getDatabase()
                .orderDao()
                .getAllOrdersWithDetails()
        }.map { it.toSummary() }
    }

    private fun OrderWithDetails.toSummary(): OrderSummary {
        return OrderSummary(
            id = order.id,
            customerName = customer?.fullName ?: "Без клиента",
            managerName = manager?.fullName ?: "Не назначен",
            statusName = status.name,
            orderDate = order.orderDate,
            comment = order.comment,
            itemsCount = items.sumOf { it.quantity },
            totalAmount = items.fold(BigDecimal.ZERO) { total, item ->
                total + item.unitPrice.multiply(BigDecimal(item.quantity))
                    .multiply(BigDecimal.ONE.subtract(item.discountPercent.divide(BigDecimal(100))))
            },
        )
    }
}
