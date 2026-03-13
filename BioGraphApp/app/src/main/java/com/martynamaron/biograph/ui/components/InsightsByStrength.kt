package com.martynamaron.biograph.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.martynamaron.biograph.data.analysis.InsightWithTrend
import com.martynamaron.biograph.viewmodel.StrengthTier

@Composable
fun InsightsByStrength(
    insights: List<InsightWithTrend>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        insights.forEach { item ->
            InsightCard(
                insight = item.insight,
                strengthTier = StrengthTier.fromCoefficient(item.insight.correlationCoefficient),
                trend = item.trend
            )
        }
    }
}
