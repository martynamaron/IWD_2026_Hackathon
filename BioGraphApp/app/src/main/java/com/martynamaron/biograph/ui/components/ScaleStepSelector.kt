package com.martynamaron.biograph.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ScaleStepSelector(
    selectedValue: Int?,
    onSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (step in 0..5) {
            val isSelected = selectedValue == step
            val containerColor by animateColorAsState(
                targetValue = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surface,
                animationSpec = tween(200),
                label = "step_color_$step"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isSelected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurface,
                animationSpec = tween(200),
                label = "step_content_color_$step"
            )

            if (isSelected) {
                FilledTonalButton(
                    onClick = { onSelect(null) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = containerColor,
                        contentColor = contentColor
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(text = step.toString())
                }
            } else {
                OutlinedButton(
                    onClick = { onSelect(step) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(text = step.toString())
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ScaleStepSelectorSelectedPreview() {
    MaterialTheme {
        ScaleStepSelector(selectedValue = 3, onSelect = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ScaleStepSelectorNullPreview() {
    MaterialTheme {
        ScaleStepSelector(selectedValue = null, onSelect = {})
    }
}
