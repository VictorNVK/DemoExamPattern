package ru.demoexam.template.data

import kotlinx.coroutines.runBlocking
import ru.demoexam.template.api.BackendClientProvider
import ru.demoexam.template.model.OrderSummary

class OrderRepository {
    fun findAll(): List<OrderSummary> {
        return runBlocking {
            BackendClientProvider.getClient().getOrders()
        }
    }
}
