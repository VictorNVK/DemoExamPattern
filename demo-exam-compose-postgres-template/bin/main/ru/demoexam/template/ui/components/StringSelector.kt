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

@Composable
fun StringSelector(
    label: String,
    selectedValue: String?,
    items: List<String>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dialogOpen by remember { mutableStateOf(false) }
    val displayText = selectedValue?.takeIf { it.isNotBlank() }
        ?: when {
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
            items = items,
            onSelect = {
                onSelected(it)
                dialogOpen = false
            },
            onDismiss = { dialogOpen = false },
        )
    }
}
