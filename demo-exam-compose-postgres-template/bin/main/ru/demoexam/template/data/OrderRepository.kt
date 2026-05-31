package ru.demoexam.template.data

import kotlinx.coroutines.runBlocking
import ru.demoexam.template.api.BackendClientProvider
import ru.demoexam.template.model.OrderDetail
import ru.demoexam.template.model.OrderEditorPayload
import ru.demoexam.template.model.OrderSummary

class OrderRepository {
    fun findAll(): List<OrderSummary> {
        return runBlocking {
            BackendClientProvider.getClient().getOrders()
        }
    }

    fun findById(orderId: Int): OrderDetail? {
        return runBlocking {
            BackendClientProvider.getClient().getOrder(orderId)
        }
    }

    fun nextId(): Int {
        return runBlocking {
            BackendClientProvider.getClient().nextOrderId()
        }
    }

    fun loadEditorPayload(draft: OrderDetail): OrderEditorPayload {
        return runBlocking {
            BackendClientProvider.getClient().loadOrderEditorPayload(draft)
        }
    }

    fun save(order: OrderDetail, isNew: Boolean): OrderDetail {
        return runBlocking {
            BackendClientProvider.getClient().saveOrder(order, isNew)
        }
    }

    fun delete(orderId: Int): Boolean {
        return runBlocking {
            BackendClientProvider.getClient().deleteOrder(orderId)
        }
    }
}
