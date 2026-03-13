package com.martynamaron.biograph.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.martynamaron.biograph.data.InputType

@Composable
fun InputTypeSelector(
    selected: InputType?,
    onSelect: (InputType) -> Unit,
    locked: Boolean,
    modifier: Modifier = Modifier
) {
    if (locked && selected != null) {
        Text(
            text = when (selected) {
                InputType.TOGGLE -> "Toggle"
                InputType.SCALE -> "Scale (0–5)"
                InputType.MULTIPLE_CHOICE -> "Multiple Choice"
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(vertical = 8.dp)
        )
    } else {
        Column(modifier = modifier.fillMaxWidth()) {
            InputType.entries.forEach { type ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selected == type,
                        onClick = { onSelect(type) }
                    )
                    Spacer(modifier = Modifier.padding(start = 4.dp))
                    Text(
                        text = when (type) {
                            InputType.TOGGLE -> "Toggle"
                            InputType.SCALE -> "Scale (0–5)"
                            InputType.MULTIPLE_CHOICE -> "Multiple Choice"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InputTypeSelectorPreview() {
    MaterialTheme {
        InputTypeSelector(
            selected = InputType.SCALE,
            onSelect = {},
            locked = false
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InputTypeSelectorLockedPreview() {
    MaterialTheme {
        InputTypeSelector(
            selected = InputType.MULTIPLE_CHOICE,
            onSelect = {},
            locked = true
        )
    }
}
