package com.martynamaron.biograph.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.martynamaron.biograph.data.local.InsightEntity

@Composable
fun InsightCard(insight: InsightEntity, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    ) {
        Text(
            text = insight.insightText,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InsightCardPreview() {
    InsightCard(
        insight = InsightEntity(
            id = 1,
            dataType1Id = 1,
            dataType2Id = 2,
            correlationCoefficient = 0.72,
            correlationMethod = "PHI",
            insightText = "🏃 Exercise occurred on 75% of days you also had 😊 Good mood",
            sampleSize = 30
        )
    )
}
