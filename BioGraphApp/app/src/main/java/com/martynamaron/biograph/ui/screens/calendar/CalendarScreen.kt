package com.martynamaron.biograph.ui.screens.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.martynamaron.biograph.BioGraphApplication
import com.martynamaron.biograph.ui.components.CalendarDay
import com.martynamaron.biograph.viewmodel.CalendarViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateToDataTypes: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: CalendarViewModel = viewModel(
        factory = run {
            val app = LocalContext.current.applicationContext as BioGraphApplication
            CalendarViewModel.Factory(app.dataTypeRepository, app.dailyEntryRepository, app.multipleChoiceRepository)
        }
    )
) {
    val currentMonth by viewModel.currentMonth.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val datesWithEntries by viewModel.datesWithEntries.collectAsStateWithLifecycle()
    val dataTypes by viewModel.dataTypes.collectAsStateWithLifecycle()
    val entriesForSelectedDate by viewModel.entriesForSelectedDate.collectAsStateWithLifecycle()
    val scaleValues by viewModel.scaleValues.collectAsStateWithLifecycle()
    val multiChoiceSelections by viewModel.multiChoiceSelections.collectAsStateWithLifecycle()

    // Track direction for slide animation
    var slideDirection by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BioGraph") },
                actions = {
                    IconButton(onClick = onNavigateToDataTypes) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Data Types")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 8.dp)
        ) {
            // Month navigation header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    slideDirection = -1
                    viewModel.navigateToPreviousMonth()
                }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month")
                }
                Text(
                    text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(onClick = {
                    slideDirection = 1
                    viewModel.navigateToNextMonth()
                }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next month")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Day-of-week headers
            Row(modifier = Modifier.fillMaxWidth()) {
                val daysOfWeek = DayOfWeek.entries
                daysOfWeek.forEach { day ->
                    Text(
                        text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Calendar grid with animated month transitions
            AnimatedContent(
                targetState = currentMonth,
                transitionSpec = {
                    val dir = slideDirection
                    slideInHorizontally(tween(300)) { fullWidth -> fullWidth * dir } togetherWith
                            slideOutHorizontally(tween(300)) { fullWidth -> -fullWidth * dir } using
                            SizeTransform(clip = false)
                },
                label = "month_transition"
            ) { month ->
                CalendarGrid(
                    month = month,
                    today = LocalDate.now(),
                    datesWithEntries = datesWithEntries.toSet(),
                    onDayClick = { date -> viewModel.selectDate(date) }
                )
            }
        }
    }

    // Day panel bottom sheet
    selectedDate?.let { date ->
        DayPanel(
            date = date,
            dataTypes = dataTypes,
            entries = entriesForSelectedDate,
            scaleValues = scaleValues,
            multiChoiceSelections = multiChoiceSelections,
            multipleChoiceRepository = (LocalContext.current.applicationContext as BioGraphApplication).multipleChoiceRepository,
            onScaleValueChanged = { dataTypeId, value -> viewModel.setScaleValue(dataTypeId, value) },
            onToggleMultiChoiceOption = { dataTypeId, optionId -> viewModel.toggleMultiChoiceOption(dataTypeId, optionId) },
            onSave = { d, activeIds, scales -> viewModel.saveEntriesForDate(d, activeIds) },
            onDismiss = { viewModel.clearSelectedDate() }
        )
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    today: LocalDate,
    datesWithEntries: Set<String>,
    onDayClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = month.atDay(1)
    // Monday = 1 (DayOfWeek.MONDAY.value), offset from start of week
    val startOffset = (firstDayOfMonth.dayOfWeek.value - DayOfWeek.MONDAY.value + 7) % 7
    val daysInMonth = month.lengthOfMonth()
    val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    // Build cells: empty cells for offset + day cells
    val cells = buildList {
        repeat(startOffset) { add(null) }
        for (day in 1..daysInMonth) {
            add(day)
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items(cells) { dayNumber ->
            if (dayNumber != null) {
                val date = month.atDay(dayNumber)
                val dateString = date.format(dateFormatter)
                CalendarDay(
                    dayNumber = dayNumber,
                    isToday = date == today,
                    hasActivity = dateString in datesWithEntries,
                    onClick = { onDayClick(date) }
                )
            } else {
                // Empty cell for offset
                Box(modifier = Modifier.size(48.dp))
            }
        }
    }
}
