package ru.demoexam.template.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import ru.demoexam.template.model.MessageKind
import ru.demoexam.template.model.UiMessage

@Composable
fun MessageDialog(
    message: UiMessage,
    onDismiss: () -> Unit,
) {
    val icon = when (message.kind) {
        MessageKind.INFO -> Icons.Default.Info
        MessageKind.WARNING -> Icons.Default.WarningAmber
        MessageKind.ERROR -> Icons.Default.ErrorOutline
    }

    val tint = when (message.kind) {
        MessageKind.INFO -> MaterialTheme.colorScheme.primary
        MessageKind.WARNING -> MaterialTheme.colorScheme.secondary
        MessageKind.ERROR -> MaterialTheme.colorScheme.error
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        },
        title = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                )
                Text(message.title)
            }
        },
        text = {
            Text(message.text)
        },
    )
}
