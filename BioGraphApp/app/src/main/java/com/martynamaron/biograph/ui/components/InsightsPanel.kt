package com.martynamaron.biograph.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.martynamaron.biograph.data.analysis.InsightWithTrend
import com.martynamaron.biograph.data.local.InsightEntity
import com.martynamaron.biograph.ui.theme.MyApplicationTheme
import com.martynamaron.biograph.viewmodel.DataTypeInsightGroup
import com.martynamaron.biograph.viewmodel.GroupedInsight
import com.martynamaron.biograph.viewmodel.InsightPanelState
import com.martynamaron.biograph.viewmodel.InsightPeriod
import com.martynamaron.biograph.viewmodel.InsightSortMode
import com.martynamaron.biograph.viewmodel.InsightSortState

@Composable
fun InsightsPanel(
    state: InsightPanelState,
    modifier: Modifier = Modifier,
    selectedPeriod: InsightPeriod = InsightPeriod.LAST_3_MONTHS,
    onPeriodSelected: (InsightPeriod) -> Unit = {},
    sortMode: InsightSortMode = InsightSortMode.BY_STRENGTH,
    sortState: InsightSortState = InsightSortState.ByStrength(emptyList()),
    onSortModeSelected: (InsightSortMode) -> Unit = {},
) {
    if (state is InsightPanelState.Hidden) return

    var showInfoSheet by remember { mutableStateOf<InsightSortMode?>(null) }

    if (showInfoSheet != null) {
        @OptIn(ExperimentalMaterial3Api::class)
        ModalBottomSheet(
            onDismissRequest = { showInfoSheet = null },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 32.dp)
            ) {
                Text(
                    text = when (showInfoSheet) {
                        InsightSortMode.BY_STRENGTH -> "By Strength"
                        InsightSortMode.BY_DATA_TYPE -> "By Data Type"
                        else -> ""
                    },
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Text(
                    text = when (showInfoSheet) {
                        InsightSortMode.BY_STRENGTH -> "Sorts insights by how strong the correlation is. " +
                            "Strong correlations appear first — these are the patterns most consistently found in your data."
                        InsightSortMode.BY_DATA_TYPE -> "Groups insights by the data types involved. " +
                            "This lets you see all patterns related to a specific thing you track, like sleep or exercise."
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Time period tabs
        val periods = InsightPeriod.entries
        val selectedIndex = periods.indexOf(selectedPeriod)
        @OptIn(ExperimentalMaterial3Api::class)
        PrimaryTabRow(
            selectedTabIndex = selectedIndex,
            containerColor = Transparent,
            modifier = Modifier.fillMaxWidth()
        ) {
            periods.forEach { period ->
                Tab(
                    selected = period == selectedPeriod,
                    onClick = { onPeriodSelected(period) },
                    text = {
                        Text(
                            text = period.label,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                )
            }
        }

        // Sort mode toggle — only visible when insights are available
        if (state is InsightPanelState.Success) {
            val modes = InsightSortMode.entries
            val selectedModeIndex = modes.indexOf(sortMode)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.weight(1f)
                ) {
                    modes.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = index == selectedModeIndex,
                            onClick = { onSortModeSelected(mode) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = modes.size
                            )
                        ) {
                            Text(
                                text = when (mode) {
                                    InsightSortMode.BY_STRENGTH -> "By Strength"
                                    InsightSortMode.BY_DATA_TYPE -> "By Data Type"
                                },
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
                IconButton(
                    onClick = { showInfoSheet = sortMode },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Sort mode info",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        AnimatedContent(
            targetState = state,
            transitionSpec = {
                fadeIn(tween(300, easing = EaseInOutCubic)) togetherWith
                        fadeOut(tween(300, easing = EaseInOutCubic))
            },
            label = "insights_panel_transition",
            modifier = Modifier.fillMaxWidth()
        ) { targetState ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (targetState) {
                    is InsightPanelState.Loading -> {
                        InsightLoader()
                    }
                    is InsightPanelState.Success -> {
                        Text(
                            text = "Insights",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        )

                        // Sort mode animated content
                        AnimatedContent(
                            targetState = sortState,
                            transitionSpec = {
                                fadeIn(tween(300, easing = EaseInOutCubic)) togetherWith
                                        fadeOut(tween(300, easing = EaseInOutCubic))
                            },
                            label = "sort_mode_transition"
                        ) { currentSortState ->
                            when (currentSortState) {
                                is InsightSortState.ByStrength -> {
                                    InsightsByStrength(insights = currentSortState.insights)
                                }
                                is InsightSortState.ByDataType -> {
                                    InsightsByDataType(groups = currentSortState.groups)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "These are patterns in your data, not medical advice. Correlation does not imply causation.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    is InsightPanelState.InsufficientData -> {
                        Text(
                            text = targetState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                    is InsightPanelState.Error -> {
                        Text(
                            text = targetState.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                    is InsightPanelState.Hidden -> { /* handled above */ }
                }
            }
        }
    } // end outer Column
}

@Preview(showBackground = true)
@Composable
private fun InsightsPanelLoadingPreview() {
    MyApplicationTheme {
        InsightsPanel(state = InsightPanelState.Loading)
    }
}

@Preview(showBackground = true)
@Composable
private fun InsightsPanelSuccessPreview() {
    val insights = listOf(
        InsightEntity(
            id = 1, dataType1Id = 1, dataType2Id = 2,
            correlationCoefficient = 0.85, correlationMethod = "PHI",
            insightText = "\uD83C\uDFC3 Exercise occurred on 85% of days you also had \uD83D\uDE0A Good mood",
            sampleSize = 30
        ),
        InsightEntity(
            id = 2, dataType1Id = 3, dataType2Id = 4,
            correlationCoefficient = 0.65, correlationMethod = "POINT_BISERIAL",
            insightText = "Your \uD83D\uDE34 Sleep quality tended to be lower on days with \u2615 Coffee (avg 2.1 vs 3.8)",
            sampleSize = 28
        )
    )
    MyApplicationTheme {
        InsightsPanel(
            state = InsightPanelState.Success(insights = insights),
            sortMode = InsightSortMode.BY_STRENGTH,
            sortState = InsightSortState.ByStrength(insights.map { InsightWithTrend(it, null) })
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InsightsPanelByDataTypePreview() {
    val insight1 = InsightEntity(
        id = 1, dataType1Id = 1, dataType2Id = 2,
        correlationCoefficient = 0.85, correlationMethod = "PHI",
        insightText = "\uD83C\uDFC3 Exercise occurred on 85% of days with \uD83D\uDE0A Good mood",
        sampleSize = 30
    )
    MyApplicationTheme {
        InsightsPanel(
            state = InsightPanelState.Success(insights = listOf(insight1)),
            sortMode = InsightSortMode.BY_DATA_TYPE,
            sortState = InsightSortState.ByDataType(
                groups = listOf(
                    DataTypeInsightGroup(
                        dataTypeName = "\uD83C\uDFC3 Exercise",
                        insightCount = 1,
                        insights = listOf(GroupedInsight(insight1, "\uD83D\uDE0A Good mood"))
                    )
                )
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InsightsPanelInsufficientDataPreview() {
    MyApplicationTheme {
        InsightsPanel(
            state = InsightPanelState.InsufficientData(
                message = "Keep tracking! Insights will appear once there's enough data to find patterns."
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InsightsPanelErrorPreview() {
    MyApplicationTheme {
        InsightsPanel(
            state = InsightPanelState.Error(
                message = "Couldn't generate insights right now. Try again later."
            )
        )
    }
}
