package com.martynamaron.biograph.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.martynamaron.biograph.ui.theme.MyApplicationTheme

data class OptionUiModel(
    val key: Int,
    val emoji: String,
    val label: String
)

@Composable
fun MultiChoiceOptionEditor(
    options: List<OptionUiModel>,
    onOptionEmojiChanged: (Int, String) -> Unit,
    onOptionLabelChanged: (Int, String) -> Unit,
    onRemoveOption: (Int) -> Unit,
    onAddOption: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Options",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        options.forEach { option ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    EmojiPicker(
                        emoji = option.emoji,
                        onEmojiChanged = { onOptionEmojiChanged(option.key, it) },
                        isError = false,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    OutlinedTextField(
                        value = option.label,
                        onValueChange = { newLabel ->
                            if (newLabel.length <= 30) {
                                onOptionLabelChanged(option.key, newLabel)
                            }
                        },
                        label = { Text("Label") },
                        singleLine = true,
                        supportingText = { Text("${option.label.length}/30") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { onRemoveOption(option.key) },
                        enabled = options.size > 2,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove option",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        TextButton(
            onClick = onAddOption,
            enabled = options.size < 10
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text("Add option")
        }

        if (options.size >= 10) {
            Text(
                text = "Maximum 10 options reached",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MultiChoiceOptionEditorPreview() {
    MyApplicationTheme {
        MultiChoiceOptionEditor(
            options = listOf(
                OptionUiModel(key = 0, emoji = "🏃", label = "Running"),
                OptionUiModel(key = 1, emoji = "🏊", label = "Swimming"),
                OptionUiModel(key = 2, emoji = "🚴", label = "Cycling")
            ),
            onOptionEmojiChanged = { _, _ -> },
            onOptionLabelChanged = { _, _ -> },
            onRemoveOption = {},
            onAddOption = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
