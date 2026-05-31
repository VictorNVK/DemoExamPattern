package ru.demoexam.template.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.demoexam.template.model.OrderEditorPayload
import ru.demoexam.template.model.OrderFormModel
import ru.demoexam.template.model.toFormModel
import ru.demoexam.template.ui.components.IntLookupSelector
import ru.demoexam.template.ui.components.StringSelector

@Composable
fun OrderEditorScreen(
    payload: OrderEditorPayload,
    onSave: (OrderFormModel) -> Unit,
    onCancel: () -> Unit,
    onProductSelected: (Int) -> Pair<String, String>?,
) {
    var form by remember(payload) {
        mutableStateOf(payload.draft.toFormModel())
    }
    var lastFilledProductId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(form.productId) {
        val productId = form.productId ?: return@LaunchedEffect
        if (productId == lastFilledProductId) {
            return@LaunchedEffect
        }
        lastFilledProductId = productId
        val prices = onProductSelected(productId) ?: return@LaunchedEffect
        form = form.copy(
            unitPriceText = prices.first,
            discountText = prices.second,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = if (payload.draft.customerName.isBlank()) "Добавление заказа" else "Редактирование заказа",
            style = MaterialTheme.typography.headlineMedium,
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                OutlinedTextField(
                    value = form.id.toString(),
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("ID заказа") },
                    readOnly = true,
                    enabled = false,
                )
                OutlinedTextField(
                    value = form.customerName,
                    onValueChange = { form = form.copy(customerName = it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Клиент") },
                    singleLine = true,
                )
                IntLookupSelector(
                    label = "Менеджер",
                    selectedId = form.managerId,
                    items = payload.managers,
                    onSelected = { form = form.copy(managerId = it.id) },
                    modifier = Modifier.fillMaxWidth(),
                )
                StringSelector(
                    label = "Статус",
                    selectedValue = form.status,
                    items = payload.statuses,
                    onSelected = { form = form.copy(status = it) },
                    modifier = Modifier.fillMaxWidth(),
                )
                StringSelector(
                    label = "Пункт выдачи",
                    selectedValue = form.pickupAddress,
                    items = payload.pickupAddresses,
                    onSelected = { form = form.copy(pickupAddress = it) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = form.orderDateText,
                    onValueChange = { form = form.copy(orderDateText = it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Дата заказа (дд.мм.гггг чч:мм)") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = form.deliveryDateText,
                    onValueChange = { form = form.copy(deliveryDateText = it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Дата выдачи (необязательно)") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = form.pickupCode,
                    onValueChange = { form = form.copy(pickupCode = it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Код получения") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = form.comment,
                    onValueChange = { form = form.copy(comment = it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Комментарий") },
                )
                IntLookupSelector(
                    label = "Товар",
                    selectedId = form.productId,
                    items = payload.products,
                    onSelected = { form = form.copy(productId = it.id) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = form.quantityText,
                        onValueChange = { form = form.copy(quantityText = it) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Количество") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = form.unitPriceText,
                        onValueChange = { form = form.copy(unitPriceText = it) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Цена") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = form.discountText,
                        onValueChange = { form = form.copy(discountText = it) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Скидка, %") },
                        singleLine = true,
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { onSave(form) }) {
                Text("Сохранить")
            }
            Button(onClick = onCancel) {
                Text("Отмена")
            }
        }
    }
}
