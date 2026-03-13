package com.martynamaron.biograph.data.analysis

import com.martynamaron.biograph.data.InputType
import com.martynamaron.biograph.data.local.DailyEntryEntity
import com.martynamaron.biograph.data.local.DataTypeEntity
import com.martynamaron.biograph.data.local.MultiChoiceSelectionEntity
import com.martynamaron.biograph.data.local.MultipleChoiceOptionEntity
import kotlin.math.abs
import kotlin.math.sqrt

class CorrelationEngine {

    data class CorrelationResult(
        val dataType1Id: Long,
        val dataType2Id: Long,
        val optionId: Long?,
        val coefficient: Double,
        val method: String,
        val dataType1Emoji: String,
        val dataType1Desc: String,
        val dataType2Emoji: String,
        val dataType2Desc: String,
        val optionEmoji: String? = null,
        val optionLabel: String? = null,
        val mean1: Double? = null,
        val mean0: Double? = null,
        val coOccurrencePct: Double? = null,
        val sampleSize: Int
    )

    private data class Signal(
        val dataTypeId: Long,
        val optionId: Long?,
        val type: InputType,
        val values: Map<String, Double>,
        val emoji: String,
        val desc: String,
        val optionEmoji: String? = null,
        val optionLabel: String? = null
    )

    fun analyseAll(
        entries: List<DailyEntryEntity>,
        selections: List<MultiChoiceSelectionEntity>,
        dataTypes: List<DataTypeEntity>,
        options: Map<Long, List<MultipleChoiceOptionEntity>>,
        dates: List<String>
    ): List<CorrelationResult> {
        if (dates.size < 7 || dataTypes.size < 2) return emptyList()

        // Build day-indexed data structures
        val entriesByDate = entries.groupBy { it.date }
        val selectionsByDate = selections.groupBy { it.date }
        val dataTypeMap = dataTypes.associateBy { it.id }

        // Build signals: for each data type, a map of date → value
        // Toggle: 1.0 if present, 0.0 if absent
        // Scale: scaleValue as Double (0-5)
        // MC: each option becomes a separate binary signal

        val signals = mutableListOf<Signal>()

        for (dt in dataTypes) {
            val inputType = try {
                InputType.valueOf(dt.inputType)
            } catch (_: IllegalArgumentException) {
                continue
            }

            when (inputType) {
                InputType.TOGGLE -> {
                    val values = mutableMapOf<String, Double>()
                    for (date in dates) {
                        val present = entriesByDate[date]?.any { it.dataTypeId == dt.id } == true
                        values[date] = if (present) 1.0 else 0.0
                    }
                    signals.add(Signal(dt.id, null, InputType.TOGGLE, values, dt.emoji, dt.description))
                }
                InputType.SCALE -> {
                    val values = mutableMapOf<String, Double>()
                    for (date in dates) {
                        val entry = entriesByDate[date]?.find { it.dataTypeId == dt.id }
                        if (entry?.scaleValue != null) {
                            values[date] = entry.scaleValue.toDouble()
                        }
                    }
                    if (values.isNotEmpty()) {
                        signals.add(Signal(dt.id, null, InputType.SCALE, values, dt.emoji, dt.description))
                    }
                }
                InputType.MULTIPLE_CHOICE -> {
                    val dtOptions = options[dt.id] ?: continue
                    for (opt in dtOptions) {
                        val values = mutableMapOf<String, Double>()
                        for (date in dates) {
                            val selected = selectionsByDate[date]?.any { it.optionId == opt.id } == true
                            values[date] = if (selected) 1.0 else 0.0
                        }
                        signals.add(Signal(dt.id, opt.id, InputType.TOGGLE, values, dt.emoji, dt.description, opt.emoji, opt.label))
                    }
                }
            }
        }

        // Compute pairwise correlations
        val results = mutableListOf<CorrelationResult>()

        for (i in signals.indices) {
            for (j in i + 1 until signals.size) {
                val s1 = signals[i]
                val s2 = signals[j]

                // Skip same data type (unless different MC options)
                if (s1.dataTypeId == s2.dataTypeId && s1.optionId == null && s2.optionId == null) continue

                val result = when {
                    s1.type == InputType.TOGGLE && s2.type == InputType.TOGGLE ->
                        computePhi(s1, s2, dates)
                    s1.type == InputType.TOGGLE && s2.type == InputType.SCALE ->
                        computePointBiserial(s1, s2, dates)
                    s1.type == InputType.SCALE && s2.type == InputType.TOGGLE ->
                        computePointBiserial(s2, s1, dates)?.let {
                            it.copy(
                                dataType1Id = s1.dataTypeId, dataType2Id = s2.dataTypeId,
                                dataType1Emoji = s1.emoji, dataType1Desc = s1.desc,
                                dataType2Emoji = s2.emoji, dataType2Desc = s2.desc,
                                optionId = s1.optionId ?: s2.optionId,
                                optionEmoji = s1.optionEmoji ?: s2.optionEmoji,
                                optionLabel = s1.optionLabel ?: s2.optionLabel
                            )
                        }
                    s1.type == InputType.SCALE && s2.type == InputType.SCALE ->
                        computePearson(s1, s2, dates)
                    else -> null
                }

                if (result != null && abs(result.coefficient) >= COEFFICIENT_THRESHOLD) {
                    results.add(result)
                }
            }
        }

        return results.sortedByDescending { abs(it.coefficient) }
    }

    private fun computePhi(
        s1: Signal,
        s2: Signal,
        dates: List<String>
    ): CorrelationResult? {
        var a = 0; var b = 0; var c = 0; var d = 0

        for (date in dates) {
            val v1 = (s1.values[date] ?: 0.0) > 0.5
            val v2 = (s2.values[date] ?: 0.0) > 0.5
            when {
                v1 && v2 -> a++
                v1 && !v2 -> b++
                !v1 && v2 -> c++
                else -> d++
            }
        }

        val denom = sqrt(((a + b).toDouble() * (c + d) * (a + c) * (b + d)))
        if (denom == 0.0) return null

        val phi = (a.toDouble() * d - b.toDouble() * c) / denom
        val coOccurrence = if (a + b > 0) (a.toDouble() / (a + b)) * 100.0 else 0.0

        return CorrelationResult(
            dataType1Id = s1.dataTypeId, dataType2Id = s2.dataTypeId,
            optionId = s1.optionId ?: s2.optionId,
            coefficient = phi, method = "PHI",
            dataType1Emoji = s1.emoji, dataType1Desc = s1.desc,
            dataType2Emoji = s2.emoji, dataType2Desc = s2.desc,
            optionEmoji = s1.optionEmoji ?: s2.optionEmoji,
            optionLabel = s1.optionLabel ?: s2.optionLabel,
            coOccurrencePct = coOccurrence,
            sampleSize = dates.size
        )
    }

    private fun computePointBiserial(
        binarySignal: Signal,
        scaleSignal: Signal,
        dates: List<String>
    ): CorrelationResult? {
        val group1 = mutableListOf<Double>() // binary = 1
        val group0 = mutableListOf<Double>() // binary = 0

        for (date in dates) {
            val scaleVal = scaleSignal.values[date] ?: continue
            val binaryVal = (binarySignal.values[date] ?: 0.0) > 0.5
            if (binaryVal) group1.add(scaleVal) else group0.add(scaleVal)
        }

        if (group1.isEmpty() || group0.isEmpty()) return null

        val mean1 = group1.average()
        val mean0 = group0.average()
        val n = (group1.size + group0.size).toDouble()
        val allValues = group1 + group0
        val overallMean = allValues.average()
        val variance = allValues.sumOf { (it - overallMean) * (it - overallMean) } / n
        val sd = sqrt(variance)

        if (sd == 0.0) return null

        val rpb = ((mean1 - mean0) / sd) * sqrt((group1.size * group0.size) / (n * n))

        return CorrelationResult(
            dataType1Id = binarySignal.dataTypeId, dataType2Id = scaleSignal.dataTypeId,
            optionId = binarySignal.optionId,
            coefficient = rpb, method = "POINT_BISERIAL",
            dataType1Emoji = binarySignal.emoji, dataType1Desc = binarySignal.desc,
            dataType2Emoji = scaleSignal.emoji, dataType2Desc = scaleSignal.desc,
            optionEmoji = binarySignal.optionEmoji, optionLabel = binarySignal.optionLabel,
            mean1 = mean1, mean0 = mean0,
            sampleSize = group1.size + group0.size
        )
    }

    private fun computePearson(
        s1: Signal,
        s2: Signal,
        dates: List<String>
    ): CorrelationResult? {
        val pairs = dates.mapNotNull { date ->
            val v1 = s1.values[date] ?: return@mapNotNull null
            val v2 = s2.values[date] ?: return@mapNotNull null
            v1 to v2
        }

        if (pairs.size < 7) return null

        val mean1 = pairs.map { it.first }.average()
        val mean2 = pairs.map { it.second }.average()

        var sumXY = 0.0; var sumX2 = 0.0; var sumY2 = 0.0
        for ((x, y) in pairs) {
            val dx = x - mean1
            val dy = y - mean2
            sumXY += dx * dy
            sumX2 += dx * dx
            sumY2 += dy * dy
        }

        val denom = sqrt(sumX2 * sumY2)
        if (denom == 0.0) return null

        val r = sumXY / denom

        return CorrelationResult(
            dataType1Id = s1.dataTypeId, dataType2Id = s2.dataTypeId,
            optionId = null,
            coefficient = r, method = "PEARSON",
            dataType1Emoji = s1.emoji, dataType1Desc = s1.desc,
            dataType2Emoji = s2.emoji, dataType2Desc = s2.desc,
            sampleSize = pairs.size
        )
    }

    companion object {
        const val COEFFICIENT_THRESHOLD = 0.35
    }
}
