package com.martynamaron.biograph.ui.screens.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.martynamaron.biograph.data.local.DailyEntryEntity
import com.martynamaron.biograph.data.local.DataTypeEntity
import com.martynamaron.biograph.ui.components.DataTypeToggleItem
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayPanel(
    date: LocalDate,
    dataTypes: List<DataTypeEntity>,
    entries: List<DailyEntryEntity>,
    onSave: (LocalDate, Set<Long>) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val activeIds = remember(entries) {
        mutableStateMapOf<Long, Boolean>().apply {
            val entryTypeIds = entries.map { it.dataTypeId }.toSet()
            dataTypes.forEach { dt ->
                put(dt.id, dt.id in entryTypeIds)
            }
        }
    }

    val displayDate = date.format(
        DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = displayDate,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (dataTypes.isEmpty()) {
                Text(
                    text = "No data types yet. Create one first!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    items(dataTypes, key = { it.id }) { dataType ->
                        DataTypeToggleItem(
                            emoji = dataType.emoji,
                            description = dataType.description,
                            isChecked = activeIds[dataType.id] == true,
                            onToggle = { checked -> activeIds[dataType.id] = checked }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val selected = activeIds.filterValues { it }.keys
                        onSave(date, selected)
                        scope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Text("Save")
                }
            }
        }
    }
}
