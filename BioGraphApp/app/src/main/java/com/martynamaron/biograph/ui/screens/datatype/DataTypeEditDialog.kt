package com.martynamaron.biograph.ui.screens.datatype

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.martynamaron.biograph.data.local.DataTypeEntity
import com.martynamaron.biograph.ui.components.EmojiPicker

@Composable
fun DataTypeEditDialog(
    emoji: String,
    description: String,
    emojiError: String?,
    descriptionError: String?,
    saveError: String?,
    editingDataType: DataTypeEntity?,
    onEmojiChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (editingDataType != null) "Edit Data Type" else "New Data Type")
        },
        text = {
            Column {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EmojiPicker(
                        emoji = emoji,
                        onEmojiChanged = onEmojiChanged,
                        isError = emojiError != null
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = onDescriptionChanged,
                        label = { Text("Description") },
                        placeholder = { Text("e.g. Period / bleeding") },
                        singleLine = true,
                        isError = descriptionError != null,
                        supportingText = {
                            Text("${description.length}/60")
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (emojiError != null) {
                    Text(
                        text = emojiError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if (descriptionError != null) {
                    Text(
                        text = descriptionError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if (saveError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = saveError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
