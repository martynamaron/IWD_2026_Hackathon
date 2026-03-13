package com.martynamaron.biograph.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.martynamaron.biograph.data.local.InsightEntity
import com.martynamaron.biograph.ui.theme.MyApplicationTheme
import com.martynamaron.biograph.viewmodel.DataTypeInsightGroup
import com.martynamaron.biograph.viewmodel.GroupedInsight
import com.martynamaron.biograph.viewmodel.StrengthTier

@Composable
fun InsightsByDataType(
    groups: List<DataTypeInsightGroup>,
    modifier: Modifier = Modifier
) {
    val expandedState = remember { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        groups.forEach { group ->
            val isExpanded = expandedState.getOrElse(group.dataTypeName) { true }

            // Group header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedState[group.dataTypeName] = !isExpanded }
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${group.dataTypeName} (${group.insightCount})",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Collapsible group body
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(tween(300, easing = EaseInOutCubic)),
                exit = shrinkVertically(tween(300, easing = EaseInOutCubic))
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    group.insights.forEach { groupedInsight ->
                        InsightCard(
                            insight = groupedInsight.insight,
                            strengthTier = StrengthTier.fromCoefficient(groupedInsight.insight.correlationCoefficient),
                            alsoInDataType = groupedInsight.alsoInDataTypeName,
                            trend = groupedInsight.trend
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InsightsByDataTypePreview() {
    val insight1 = InsightEntity(
        id = 1, dataType1Id = 1, dataType2Id = 2,
        correlationCoefficient = 0.85, correlationMethod = "PHI",
        insightText = "🏃 Exercise occurred on 85% of days with 😊 Good mood",
        sampleSize = 30
    )
    val insight2 = InsightEntity(
        id = 2, dataType1Id = 1, dataType2Id = 3,
        correlationCoefficient = 0.65, correlationMethod = "PEARSON",
        insightText = "🏃 Exercise correlated moderately with 😴 Sleep quality",
        sampleSize = 25
    )
    MyApplicationTheme {
        InsightsByDataType(
            groups = listOf(
                DataTypeInsightGroup(
                    dataTypeName = "🏃 Exercise",
                    insightCount = 2,
                    insights = listOf(
                        GroupedInsight(insight1, alsoInDataTypeName = "😊 Good mood"),
                        GroupedInsight(insight2, alsoInDataTypeName = "😴 Sleep quality")
                    )
                ),
                DataTypeInsightGroup(
                    dataTypeName = "😊 Good mood",
                    insightCount = 1,
                    insights = listOf(
                        GroupedInsight(insight1, alsoInDataTypeName = "🏃 Exercise")
                    )
                )
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InsightsByDataTypeSingleGroupPreview() {
    val insight = InsightEntity(
        id = 1, dataType1Id = 1, dataType2Id = 2,
        correlationCoefficient = 0.85, correlationMethod = "PHI",
        insightText = "🏃 Exercise occurred on 85% of days with 😊 Good mood",
        sampleSize = 30
    )
    MyApplicationTheme {
        InsightsByDataType(
            groups = listOf(
                DataTypeInsightGroup(
                    dataTypeName = "🏃 Exercise",
                    insightCount = 1,
                    insights = listOf(
                        GroupedInsight(insight, alsoInDataTypeName = "😊 Good mood")
                    )
                )
            )
        )
    }
}
