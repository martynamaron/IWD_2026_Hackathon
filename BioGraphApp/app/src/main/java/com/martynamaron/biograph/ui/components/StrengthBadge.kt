package com.martynamaron.biograph.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.martynamaron.biograph.ui.theme.MyApplicationTheme
import com.martynamaron.biograph.ui.theme.StrengthMild
import com.martynamaron.biograph.ui.theme.StrengthModerate
import com.martynamaron.biograph.ui.theme.StrengthStrong
import com.martynamaron.biograph.viewmodel.StrengthTier

@Composable
fun StrengthBadge(tier: StrengthTier, modifier: Modifier = Modifier) {
    val color = when (tier) {
        StrengthTier.STRONG -> StrengthStrong
        StrengthTier.MODERATE -> StrengthModerate
        StrengthTier.MILD -> StrengthMild
    }
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = tier.label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StrengthBadgeStrongPreview() {
    MyApplicationTheme {
        StrengthBadge(tier = StrengthTier.STRONG)
    }
}

@Preview(showBackground = true)
@Composable
private fun StrengthBadgeModeratePreview() {
    MyApplicationTheme {
        StrengthBadge(tier = StrengthTier.MODERATE)
    }
}
