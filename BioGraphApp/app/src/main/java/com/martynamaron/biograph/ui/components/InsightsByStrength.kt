package com.martynamaron.biograph.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.martynamaron.biograph.data.local.InsightEntity
import com.martynamaron.biograph.ui.theme.MyApplicationTheme
import com.martynamaron.biograph.viewmodel.StrengthTier

@Composable
fun InsightsByStrength(
    insights: List<InsightEntity>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        insights.forEach { insight ->
            InsightCard(
                insight = insight,
                strengthTier = StrengthTier.fromCoefficient(insight.correlationCoefficient)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InsightsByStrengthPreview() {
    MyApplicationTheme {
        InsightsByStrength(
            insights = listOf(
                InsightEntity(
                    id = 1, dataType1Id = 1, dataType2Id = 2,
                    correlationCoefficient = 0.85, correlationMethod = "PHI",
                    insightText = "🏃 Exercise occurred on 85% of days with 😊 Good mood",
                    sampleSize = 30
                ),
                InsightEntity(
                    id = 2, dataType1Id = 3, dataType2Id = 4,
                    correlationCoefficient = 0.65, correlationMethod = "PEARSON",
                    insightText = "🤕 Headache correlated moderately with 🏠 Staying indoors",
                    sampleSize = 25
                )
            )
        )
    }
}
