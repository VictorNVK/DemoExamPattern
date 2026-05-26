package ru.demoexam.template.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.demoexam.template.model.OrderSummary
import ru.demoexam.template.util.toCurrencyText
import ru.demoexam.template.util.toDisplayText

@Composable
fun OrderListScreen(
    orders: List<OrderSummary>,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Экран заказов",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = "Этот экран уже подключен к БД и выводит заказы. Используй его как основу под добавление, редактирование и удаление заказов в конкретном варианте задания.",
                )
            }
        }

        if (orders.isEmpty()) {
            Text("Заказов пока нет.")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(orders, key = { it.id }) { order ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = "Заказ #${order.id}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text("Дата: ${order.orderDate.toDisplayText()}")
                            Text("Клиент: ${order.customerName}")
                            Text("Менеджер: ${order.managerName}")
                            Text("Статус: ${order.statusName}")
                            Text("Позиций: ${order.itemsCount}")
                            Text("Сумма: ${order.totalAmount.toCurrencyText()}")
                            if (order.comment.isNotBlank()) {
                                Text("Комментарий: ${order.comment}")
                            }
                        }
                    }
                }
            }
        }
    }
}
