package com.martynamaron.biograph.ui.screens.datatype

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.ui.tooling.preview.Preview
import com.martynamaron.biograph.data.InputType
import com.martynamaron.biograph.data.local.DataTypeEntity
import com.martynamaron.biograph.ui.components.EmojiPicker
import com.martynamaron.biograph.ui.components.InputTypeSelector
import com.martynamaron.biograph.ui.components.MultiChoiceOptionEditor
import com.martynamaron.biograph.ui.components.OptionUiModel
import com.martynamaron.biograph.viewmodel.ConfirmMigrationState

@Composable
fun DataTypeEditDialog(
    emoji: String,
    description: String,
    inputType: InputType?,
    isInputTypeLocked: Boolean,
    options: List<OptionUiModel>,
    optionsError: String?,
    emojiError: String?,
    descriptionError: String?,
    inputTypeError: String?,
    saveError: String?,
    editingDataType: DataTypeEntity?,
    confirmMigrationState: ConfirmMigrationState,
    onEmojiChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    onInputTypeChanged: (InputType) -> Unit,
    onOptionEmojiChanged: (Int, String) -> Unit,
    onOptionLabelChanged: (Int, String) -> Unit,
    onRemoveOption: (Int) -> Unit,
    onAddOption: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    onConfirmMigration: () -> Unit,
    onDismissMigration: () -> Unit
) {
    // Migration confirmation dialog — shown on top of the edit dialog
    if (confirmMigrationState is ConfirmMigrationState.Pending) {
        AlertDialog(
            onDismissRequest = onDismissMigration,
            title = { Text("Change Input Type?") },
            text = {
                Text(
                    "This will reset ${confirmMigrationState.affectedCount} historical entries to unrecorded. " +
                        "This cannot be undone. Continue?"
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirmMigration) {
                    Text("Continue", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissMigration) {
                    Text("Cancel")
                }
            }
        )
    }

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

                Spacer(modifier = Modifier.height(12.dp))

                InputTypeSelector(
                    selected = inputType,
                    onSelect = onInputTypeChanged,
                    locked = isInputTypeLocked,
                    modifier = Modifier.fillMaxWidth()
                )

                if (inputTypeError != null) {
                    Text(
                        text = inputTypeError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                AnimatedContent(
                    targetState = inputType,
                    label = "input_type_content"
                ) { currentType ->
                    Column {
                        if (currentType == InputType.MULTIPLE_CHOICE) {
                            Spacer(modifier = Modifier.height(12.dp))
                            MultiChoiceOptionEditor(
                                options = options,
                                onOptionEmojiChanged = onOptionEmojiChanged,
                                onOptionLabelChanged = onOptionLabelChanged,
                                onRemoveOption = onRemoveOption,
                                onAddOption = onAddOption
                            )
                            if (optionsError != null) {
                                Text(
                                    text = optionsError,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
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

@Preview(showBackground = true)
@Composable
private fun DataTypeEditDialogNewTogglePreview() {
    MaterialTheme {
        DataTypeEditDialog(
            emoji = "💤",
            description = "Sleep",
            inputType = InputType.TOGGLE,
            isInputTypeLocked = false,
            options = emptyList(),
            optionsError = null,
            emojiError = null,
            descriptionError = null,
            inputTypeError = null,
            saveError = null,
            editingDataType = null,
            confirmMigrationState = ConfirmMigrationState.Hidden,
            onEmojiChanged = {},
            onDescriptionChanged = {},
            onInputTypeChanged = {},
            onOptionEmojiChanged = { _, _ -> },
            onOptionLabelChanged = { _, _ -> },
            onRemoveOption = {},
            onAddOption = {},
            onSave = {},
            onDismiss = {},
            onConfirmMigration = {},
            onDismissMigration = {}
        )
    }
}
