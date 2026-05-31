package ru.demoexam.template.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.demoexam.template.model.OrderSummary
import ru.demoexam.template.model.UserSession
import ru.demoexam.template.util.toCurrencyText
import ru.demoexam.template.util.toDisplayText

@Composable
fun OrderListScreen(
    session: UserSession,
    orders: List<OrderSummary>,
    onRefresh: () -> Unit,
    onAddOrder: () -> Unit,
    onEditOrder: (Int) -> Unit,
    onDeleteOrder: (OrderSummary) -> Unit,
) {
    var orderToDelete by remember { mutableStateOf<OrderSummary?>(null) }

    orderToDelete?.let { selectedOrder ->
        AlertDialog(
            onDismissRequest = { orderToDelete = null },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteOrder(selectedOrder)
                        orderToDelete = null
                    },
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                Button(onClick = { orderToDelete = null }) {
                    Text("Отмена")
                }
            },
            title = { Text("Подтверждение удаления") },
            text = { Text("Удалить заказ №${selectedOrder.id}?") },
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Заказы",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text("Найдено записей: ${orders.size}")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onRefresh) {
                        Text("Обновить")
                    }
                    if (session.role.canManageOrders) {
                        Button(onClick = onAddOrder) {
                            Text("Добавить заказ")
                        }
                    }
                }
            }
        }

        if (orders.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = "Заказов пока нет.",
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(orders, key = { it.id }) { order ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                enabled = session.role.canManageOrders,
                                onClick = { onEditOrder(order.id) },
                            ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = "Заказ №${order.id}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text("Дата: ${order.orderDate.toDisplayText()}")
                                order.deliveryDate?.let {
                                    Text("Выдача: ${it.toDisplayText()}")
                                }
                                Text("Клиент: ${order.customerName}")
                                Text("Менеджер: ${order.managerName}")
                                Text("Статус: ${order.statusName}")
                                Text("Пункт выдачи: ${order.pickupAddress}")
                                Text("Код: ${order.pickupCode}")
                                if (order.productArticle.isNotBlank() || order.productName.isNotBlank()) {
                                    Text("Товар: ${order.productArticle} — ${order.productName}")
                                }
                                Text("Позиций: ${order.itemsCount}")
                                Text("Сумма: ${order.totalAmount.toCurrencyText()}")
                                if (order.comment.isNotBlank()) {
                                    Text("Комментарий: ${order.comment}")
                                }
                            }
                            if (session.role.canManageOrders) {
                                Row {
                                    IconButton(onClick = { onEditOrder(order.id) }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                                    }
                                    IconButton(onClick = { orderToDelete = order }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Удалить")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
