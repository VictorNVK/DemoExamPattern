package ru.demoexam.template.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.demoexam.template.model.LookupItem
import ru.demoexam.template.model.ProductEditorPayload
import ru.demoexam.template.model.ProductFormModel
import ru.demoexam.template.ui.components.LookupSelector
import ru.demoexam.template.util.ImageStorage
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
fun ProductEditorScreen(
    payload: ProductEditorPayload,
    onSave: (ProductFormModel) -> Unit,
    onCancel: () -> Unit,
) {
    var form by remember(payload) {
        mutableStateOf(payload.draft.toFormModel())
    }

    val previewBitmap = remember(form.selectedImageSourcePath, form.existingImagePath) {
        if (form.selectedImageSourcePath != null) {
            ImageStorage.loadBitmapFromAbsolutePath(form.selectedImageSourcePath)
        } else {
            ImageStorage.loadStoredBitmap(form.existingImagePath)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = if (payload.draft.name.isBlank()) "Добавление товара" else "Редактирование товара",
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
                    label = { Text("ID товара") },
                    readOnly = true,
                    enabled = false,
                )
                OutlinedTextField(
                    value = form.name,
                    onValueChange = { form = form.copy(name = it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Наименование товара") },
                    singleLine = true,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    LookupSelector(
                        label = "Категория",
                        selectedItem = payload.categories.findById(form.categoryId),
                        items = payload.categories,
                        onSelected = { form = form.copy(categoryId = it.id) },
                        modifier = Modifier.weight(1f),
                    )
                    LookupSelector(
                        label = "Производитель",
                        selectedItem = payload.manufacturers.findById(form.manufacturerId),
                        items = payload.manufacturers,
                        onSelected = { form = form.copy(manufacturerId = it.id) },
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    LookupSelector(
                        label = "Поставщик",
                        selectedItem = payload.suppliers.findById(form.supplierId),
                        items = payload.suppliers,
                        onSelected = { form = form.copy(supplierId = it.id) },
                        modifier = Modifier.weight(1f),
                    )
                    LookupSelector(
                        label = "Единица измерения",
                        selectedItem = payload.units.findById(form.unitId),
                        items = payload.units,
                        onSelected = { form = form.copy(unitId = it.id) },
                        modifier = Modifier.weight(1f),
                    )
                }
                OutlinedTextField(
                    value = form.description,
                    onValueChange = { form = form.copy(description = it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Описание") },
                    minLines = 4,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = form.priceText,
                        onValueChange = { form = form.copy(priceText = it) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Цена") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = form.stockQuantityText,
                        onValueChange = { form = form.copy(stockQuantityText = it) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Количество на складе") },
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

                Text(
                    text = "Изображение товара будет сохранено в папку приложения `storage/images` и автоматически приведено к размеру 300x200.",
                    style = MaterialTheme.typography.bodySmall,
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                ) {
                    if (previewBitmap != null) {
                        Image(
                            bitmap = previewBitmap,
                            contentDescription = "Изображение товара",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                        )
                    } else {
                        Image(
                            painter = painterResource("assets/picture.svg"),
                            contentDescription = "Нет изображения",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            chooseImageFile()?.let { selectedPath ->
                                form = form.copy(selectedImageSourcePath = selectedPath)
                            }
                        },
                    ) {
                        Text("Выбрать изображение")
                    }
                    Button(onClick = onCancel) {
                        Text("Отмена")
                    }
                    Button(onClick = { onSave(form) }) {
                        Text("Сохранить")
                    }
                }

                form.selectedImageSourcePath?.let { selectedPath ->
                    Text(
                        text = "Выбранный файл: ${File(selectedPath).name}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

private fun List<LookupItem>.findById(id: Int?): LookupItem? {
    return firstOrNull { it.id == id }
}

private fun ru.demoexam.template.model.ProductDraft.toFormModel(): ProductFormModel {
    return ProductFormModel(
        id = id,
        name = name,
        categoryId = categoryId,
        description = description,
        manufacturerId = manufacturerId,
        supplierId = supplierId,
        unitId = unitId,
        priceText = price?.toPlainString().orEmpty(),
        stockQuantityText = stockQuantity?.toString().orEmpty(),
        discountText = discountPercent?.toPlainString().orEmpty(),
        existingImagePath = imagePath,
    )
}

private fun chooseImageFile(): String? {
    val fileChooser = JFileChooser().apply {
        dialogTitle = "Выберите изображение товара"
        fileFilter = FileNameExtensionFilter("Изображения", "png", "jpg", "jpeg", "bmp")
    }

    val result = fileChooser.showOpenDialog(null)
    return if (result == JFileChooser.APPROVE_OPTION) {
        fileChooser.selectedFile.absolutePath
    } else {
        null
    }
}
