package com.martynamaron.biograph.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.martynamaron.biograph.data.local.MultipleChoiceOptionEntity
import com.martynamaron.biograph.ui.theme.MyApplicationTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiChoiceChipRow(
    options: List<MultipleChoiceOptionEntity>,
    selectedIds: Set<Long>,
    onToggle: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options, key = { it.id }) { option ->
            FilterChip(
                selected = option.id in selectedIds,
                onClick = { onToggle(option.id) },
                label = { Text("${option.emoji} ${option.label}") }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MultiChoiceChipRowPreview() {
    MyApplicationTheme {
        MultiChoiceChipRow(
            options = listOf(
                MultipleChoiceOptionEntity(id = 1, dataTypeId = 1, emoji = "🏃", label = "Running"),
                MultipleChoiceOptionEntity(id = 2, dataTypeId = 1, emoji = "🏊", label = "Swimming"),
                MultipleChoiceOptionEntity(id = 3, dataTypeId = 1, emoji = "🚴", label = "Cycling")
            ),
            selectedIds = setOf(1L, 3L),
            onToggle = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
