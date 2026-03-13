package com.martynamaron.biograph.data.analysis

import kotlin.math.abs
import kotlin.math.roundToInt

class InsightTextGenerator {

    fun generate(result: CorrelationEngine.CorrelationResult): String {
        val e1 = result.dataType1Emoji
        val d1 = result.dataType1Desc
        val e2 = result.dataType2Emoji
        val d2 = result.dataType2Desc
        val positive = result.coefficient > 0

        return when (result.method) {
            "PHI" -> generatePhiText(result, e1, d1, e2, d2, positive)
            "POINT_BISERIAL" -> generatePointBiserialText(result, e1, d1, e2, d2, positive)
            "PEARSON" -> generatePearsonText(result, e1, d1, e2, d2, positive)
            else -> "$e1 $d1 and $e2 $d2 appear to be related"
        }
    }

    private fun generatePhiText(
        result: CorrelationEngine.CorrelationResult,
        e1: String, d1: String, e2: String, d2: String,
        positive: Boolean
    ): String {
        val pct = result.coOccurrencePct?.roundToInt() ?: (abs(result.coefficient) * 100).roundToInt()

        // If this involves an MC option, use option-specific text
        if (result.optionLabel != null) {
            val optE = result.optionEmoji ?: e1
            val optL = result.optionLabel
            return if (positive) {
                "$optE $optL occurred on $pct% of days you also had $e2 $d2"
            } else {
                "You rarely chose $optE $optL on days with $e2 $d2 ($pct% co-occurrence)"
            }
        }

        return if (positive) {
            "$e1 $d1 occurred on $pct% of days you also had $e2 $d2"
        } else {
            "You rarely had $e1 $d1 on days with $e2 $d2 ($pct% co-occurrence)"
        }
    }

    private fun generatePointBiserialText(
        result: CorrelationEngine.CorrelationResult,
        e1: String, d1: String, e2: String, d2: String,
        positive: Boolean
    ): String {
        val avg1 = result.mean1?.let { String.format("%.1f", it) } ?: "?"
        val avg0 = result.mean0?.let { String.format("%.1f", it) } ?: "?"

        // If this involves an MC option
        if (result.optionLabel != null) {
            val optE = result.optionEmoji ?: e1
            val optL = result.optionLabel
            return "On days you chose $optE $optL, your $e2 $d2 averaged $avg1 vs $avg0"
        }

        return if (positive) {
            "Your $e2 $d2 was higher on days with $e1 $d1 (avg $avg1 vs $avg0)"
        } else {
            "Your $e2 $d2 tended to be lower on days with $e1 $d1 (avg $avg1 vs $avg0)"
        }
    }

    private fun generatePearsonText(
        result: CorrelationEngine.CorrelationResult,
        e1: String, d1: String, e2: String, d2: String,
        positive: Boolean
    ): String {
        val strength = when {
            abs(result.coefficient) >= 0.7 -> "strongly"
            abs(result.coefficient) >= 0.5 -> "moderately"
            else -> "slightly"
        }
        val pct = (abs(result.coefficient) * 100).roundToInt()

        return if (positive) {
            "$e1 $d1 and $e2 $d2 $strength moved together ($pct% correlation)"
        } else {
            "$e1 $d1 and $e2 $d2 $strength moved in opposite directions ($pct% correlation)"
        }
    }
}
