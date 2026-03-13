package com.martynamaron.biograph.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.martynamaron.biograph.data.local.InsightEntity
import com.martynamaron.biograph.viewmodel.InsightPanelState
import com.martynamaron.biograph.viewmodel.InsightPeriod

@Composable
fun InsightsPanel(
    state: InsightPanelState,
    modifier: Modifier = Modifier,
    selectedPeriod: InsightPeriod = InsightPeriod.LAST_3_MONTHS,
    onPeriodSelected: (InsightPeriod) -> Unit = {},
) {
    if (state is InsightPanelState.Hidden) return

    Column(modifier = modifier.fillMaxWidth()) {
        // Time period tabs
        val periods = InsightPeriod.entries
        val selectedIndex = periods.indexOf(selectedPeriod)
        @OptIn(ExperimentalMaterial3Api::class)
        PrimaryTabRow(
            selectedTabIndex = selectedIndex,
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

    AnimatedContent(
        targetState = state,
        transitionSpec = {
            fadeIn(tween(300, easing = EaseInOutCubic)) togetherWith
                    fadeOut(tween(300, easing = EaseInOutCubic))
        },
        label = "insights_panel_transition",
        modifier = modifier.fillMaxWidth()
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
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        targetState.insights.forEach { insight ->
                            InsightCard(insight = insight)
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
    InsightsPanel(state = InsightPanelState.Loading)
}

@Preview(showBackground = true)
@Composable
private fun InsightsPanelSuccessPreview() {
    InsightsPanel(
        state = InsightPanelState.Success(
            insights = listOf(
                InsightEntity(
                    id = 1, dataType1Id = 1, dataType2Id = 2,
                    correlationCoefficient = 0.72, correlationMethod = "PHI",
                    insightText = "\uD83C\uDFC3 Exercise occurred on 75% of days you also had \uD83D\uDE0A Good mood",
                    sampleSize = 30
                ),
                InsightEntity(
                    id = 2, dataType1Id = 3, dataType2Id = 4,
                    correlationCoefficient = -0.45, correlationMethod = "POINT_BISERIAL",
                    insightText = "Your \uD83D\uDE34 Sleep quality tended to be lower on days with \u2615 Coffee (avg 2.1 vs 3.8)",
                    sampleSize = 28
                )
            )
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun InsightsPanelInsufficientDataPreview() {
    InsightsPanel(
        state = InsightPanelState.InsufficientData(
            message = "Keep tracking! Insights will appear once there's enough data to find patterns."
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun InsightsPanelErrorPreview() {
    InsightsPanel(
        state = InsightPanelState.Error(
            message = "Couldn't generate insights right now. Try again later."
        )
    )
}
