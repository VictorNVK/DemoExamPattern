package ru.demoexam.template.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.demoexam.template.model.DiscountFilter
import ru.demoexam.template.model.ProductFilter
import ru.demoexam.template.model.ProductListItem
import ru.demoexam.template.model.SortDirection
import ru.demoexam.template.model.SortField
import ru.demoexam.template.model.UserSession
import ru.demoexam.template.ui.components.OptionSelector
import ru.demoexam.template.ui.components.ProductCard
import ru.demoexam.template.ui.components.StringSelector

@Composable
fun ProductListScreen(
    session: UserSession,
    products: List<ProductListItem>,
    supplierOptions: List<String>,
    filter: ProductFilter,
    onFilterChange: (ProductFilter) -> Unit,
    onRefresh: () -> Unit,
    onOpenOrders: () -> Unit,
    onAddProduct: () -> Unit,
    onEditProduct: (Int) -> Unit,
    onDeleteProduct: (ProductListItem) -> Unit,
) {
    var productToDelete by remember { mutableStateOf<ProductListItem?>(null) }

    productToDelete?.let { selectedProduct ->
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProduct(selectedProduct)
                        productToDelete = null
                    },
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                Button(onClick = { productToDelete = null }) {
                    Text("Отмена")
                }
            },
            title = {
                Text("Подтверждение удаления")
            },
            text = {
                Text("Удалить товар «${selectedProduct.name}» без возможности восстановления?")
            },
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
                        text = "Список товаров",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text("Найдено записей: ${products.size}")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onRefresh) {
                        Text("Обновить")
                    }
                    if (session.role.canViewOrders) {
                        Button(onClick = onOpenOrders) {
                            Text("Заказы")
                        }
                    }
                    if (session.role.canManageProducts) {
                        Button(onClick = onAddProduct) {
                            Text("Добавить товар")
                        }
                    }
                }
            }
        }

        if (session.role.canSearchProducts) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = filter.searchText,
                        onValueChange = { onFilterChange(filter.copy(searchText = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Поиск по товарам") },
                        singleLine = true,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        val supplierItems = listOf("Все поставщики") + supplierOptions
                        val selectedSupplierLabel = if (filter.supplierFilter == "ALL") {
                            "Все поставщики"
                        } else {
                            filter.supplierFilter
                        }
                        StringSelector(
                            label = "Поставщик",
                            selectedValue = selectedSupplierLabel,
                            items = supplierItems,
                            onSelected = { selected ->
                                val value = if (selected == "Все поставщики") "ALL" else selected
                                onFilterChange(filter.copy(supplierFilter = value))
                            },
                            modifier = Modifier.widthIn(min = 240.dp),
                        )
                        OptionSelector(
                            label = "Фильтр по скидке",
                            selectedItem = filter.discountFilter,
                            items = DiscountFilter.entries,
                            itemText = { it.title },
                            onSelected = { onFilterChange(filter.copy(discountFilter = it)) },
                            modifier = Modifier.widthIn(min = 220.dp),
                        )
                        OptionSelector(
                            label = "Поле сортировки",
                            selectedItem = filter.sortField,
                            items = SortField.entries,
                            itemText = { it.title },
                            onSelected = { onFilterChange(filter.copy(sortField = it)) },
                            modifier = Modifier.widthIn(min = 220.dp),
                        )
                        OptionSelector(
                            label = "Направление",
                            selectedItem = filter.sortDirection,
                            items = SortDirection.entries,
                            itemText = { it.title },
                            onSelected = { onFilterChange(filter.copy(sortDirection = it)) },
                            modifier = Modifier.widthIn(min = 220.dp),
                        )
                    }
                }
            }
        }

        if (products.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = "Список товаров пуст.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(products, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        canManage = session.role.canManageProducts,
                        onEdit = { onEditProduct(product.id) },
                        onDelete = { productToDelete = product },
                    )
                }
            }
        }
    }
}
