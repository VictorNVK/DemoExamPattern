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
fun <T> OptionSelector(
    label: String,
    selectedItem: T,
    items: List<T>,
    itemText: (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var dialogOpen by remember { mutableStateOf(false) }
    val optionLabels = remember(items, itemText) { items.map(itemText) }

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
            Text(itemText(selectedItem))
        }
    }

    if (dialogOpen) {
        SelectionDialog(
            title = label,
            items = optionLabels,
            onSelect = { selectedLabel ->
                val index = optionLabels.indexOf(selectedLabel)
                if (index >= 0) {
                    onSelected(items[index])
                }
                dialogOpen = false
            },
            onDismiss = { dialogOpen = false },
        )
    }
}
