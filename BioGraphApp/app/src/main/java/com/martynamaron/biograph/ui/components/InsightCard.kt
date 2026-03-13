package com.martynamaron.biograph.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.martynamaron.biograph.data.local.InsightEntity
import com.martynamaron.biograph.ui.theme.MyApplicationTheme
import com.martynamaron.biograph.viewmodel.StrengthTier

@Composable
fun InsightCard(
    insight: InsightEntity,
    modifier: Modifier = Modifier,
    strengthTier: StrengthTier? = null,
    alsoInDataType: String? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = insight.insightText,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                if (strengthTier != null) {
                    StrengthBadge(
                        tier = strengthTier,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            if (alsoInDataType != null) {
                Text(
                    text = "Also in: $alsoInDataType",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InsightCardPreview() {
    MyApplicationTheme {
        InsightCard(
            insight = InsightEntity(
                id = 1,
                dataType1Id = 1,
                dataType2Id = 2,
                correlationCoefficient = 0.85,
                correlationMethod = "PHI",
                insightText = "🏃 Exercise occurred on 75% of days you also had 😊 Good mood",
                sampleSize = 30
            ),
            strengthTier = StrengthTier.STRONG
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InsightCardWithCrossRefPreview() {
    MyApplicationTheme {
        InsightCard(
            insight = InsightEntity(
                id = 2,
                dataType1Id = 1,
                dataType2Id = 3,
                correlationCoefficient = 0.65,
                correlationMethod = "POINT_BISERIAL",
                insightText = "🤕 Headache was present on 80% of days you didn't leave the house",
                sampleSize = 25
            ),
            strengthTier = StrengthTier.MODERATE,
            alsoInDataType = "🏠 Stayed indoors"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InsightCardStrengthTiersPreview() {
    MyApplicationTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            InsightCard(
                insight = InsightEntity(
                    id = 1,
                    dataType1Id = 1,
                    dataType2Id = 2,
                    correlationCoefficient = 0.90,
                    correlationMethod = "PHI",
                    insightText = "🏃 Exercise occurred on 90% of days you also had 😊 Good mood",
                    sampleSize = 30
                ),
                strengthTier = StrengthTier.STRONG
            )
            InsightCard(
                insight = InsightEntity(
                    id = 2,
                    dataType1Id = 3,
                    dataType2Id = 4,
                    correlationCoefficient = 0.68,
                    correlationMethod = "PEARSON",
                    insightText = "😴 Sleep quality tended to be higher on days with 🧘 Meditation",
                    sampleSize = 25
                ),
                strengthTier = StrengthTier.MODERATE
            )
            InsightCard(
                insight = InsightEntity(
                    id = 3,
                    dataType1Id = 5,
                    dataType2Id = 6,
                    correlationCoefficient = 0.45,
                    correlationMethod = "PEARSON",
                    insightText = "☕ Coffee had a mild association with 📖 Reading",
                    sampleSize = 20
                ),
                strengthTier = StrengthTier.MILD
            )
        }
    }
}
