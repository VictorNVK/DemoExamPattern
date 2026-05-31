package ru.demoexam.template.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ru.demoexam.template.model.LookupItem

@Composable
fun IntLookupSelector(
    label: String,
    selectedId: Int?,
    items: List<LookupItem>,
    onSelected: (LookupItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dialogOpen by remember { mutableStateOf(false) }
    val selectedLabel = items.firstOrNull { it.id == selectedId }?.label
    val displayText = selectedLabel ?: when {
        items.isEmpty() -> "Нет доступных значений"
        else -> "Выберите значение"
    }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
        )
        OutlinedButton(
            onClick = { dialogOpen = true },
            enabled = items.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(displayText)
        }
    }

    if (dialogOpen) {
        SelectionDialog(
            title = label,
            items = items.map { it.label },
            onSelect = { selectedLabel ->
                items.firstOrNull { it.label == selectedLabel }?.let(onSelected)
                dialogOpen = false
            },
            onDismiss = { dialogOpen = false },
        )
    }
}
