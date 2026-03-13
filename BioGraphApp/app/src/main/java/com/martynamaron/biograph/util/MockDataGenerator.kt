package com.martynamaron.biograph.util

import com.martynamaron.biograph.data.InputType
import com.martynamaron.biograph.data.local.DailyEntryEntity
import com.martynamaron.biograph.data.local.DataTypeEntity
import com.martynamaron.biograph.data.local.MultiChoiceSelectionEntity
import com.martynamaron.biograph.data.local.MultipleChoiceOptionEntity
import com.martynamaron.biograph.data.repository.DailyEntryRepository
import com.martynamaron.biograph.data.repository.DataTypeRepository
import com.martynamaron.biograph.data.repository.InsightRepository
import com.martynamaron.biograph.data.repository.MultipleChoiceRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt
import kotlin.random.Random

private val gaussianRng = java.util.Random()

class MockDataGenerator(
    private val dataTypeRepository: DataTypeRepository,
    private val dailyEntryRepository: DailyEntryRepository,
    private val multipleChoiceRepository: MultipleChoiceRepository,
    private val insightRepository: InsightRepository
) {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    // ── Data type definitions per data-model.md ──────────────────────────
    private data class DataTypeDef(
        val emoji: String,
        val description: String,
        val inputType: InputType,
        val options: List<Pair<String, String>> = emptyList() // emoji to label
    )

    private val dataTypeDefs = listOf(
        DataTypeDef("🩺", "Health", InputType.MULTIPLE_CHOICE, listOf(
            "🤕" to "headache",
            "🤢" to "stomachache",
            "😵" to "feeling dizzy",
            "🤮" to "vomiting",
            "💪" to "muscle aches",
            "😴" to "tiredness"
        )),
        DataTypeDef("💊", "Medication", InputType.TOGGLE),
        DataTypeDef("😊", "Mood", InputType.SCALE),
        DataTypeDef("⚡", "Energy Levels", InputType.SCALE),
        DataTypeDef("🏋️", "Exercise", InputType.MULTIPLE_CHOICE, listOf(
            "🚶" to "long walk",
            "🏊" to "swimming",
            "💃" to "dancing",
            "🎾" to "tennis"
        )),
        DataTypeDef("🩸", "Period Bleeding", InputType.TOGGLE)
    )

    // ── Menstrual cycle model ────────────────────────────────────────────
    private data class CycleInfo(val bleedStart: LocalDate, val bleedEnd: LocalDate)

    private fun precomputeCycles(startDate: LocalDate, endDate: LocalDate): List<CycleInfo> {
        val cycles = mutableListOf<CycleInfo>()
        // Anchor to random day in first 7 days
        var cycleStart = startDate.plusDays(Random.nextInt(0, 7).toLong())
        while (cycleStart.isBefore(endDate) || cycleStart.isEqual(endDate)) {
            val bleedDuration = Random.nextInt(4, 7) // 4-6 days
            val bleedEnd = minOf(cycleStart.plusDays(bleedDuration.toLong() - 1), endDate)
            cycles.add(CycleInfo(cycleStart, bleedEnd))
            // Next cycle: N(28, 1.5) clamped 26-30
            val cycleLen = (28.0 + gaussianRng.nextGaussian() * 1.5).roundToInt().coerceIn(26, 30)
            cycleStart = cycleStart.plusDays(cycleLen.toLong())
        }
        return cycles
    }

    private fun getCyclePhase(day: LocalDate, cycles: List<CycleInfo>): CyclePhase {
        for (cycle in cycles) {
            // Bleeding days
            if (!day.isBefore(cycle.bleedStart) && !day.isAfter(cycle.bleedEnd)) {
                val dayIndex = ChronoUnit.DAYS.between(cycle.bleedStart, day).toInt()
                return if (dayIndex <= 1) CyclePhase.EARLY_BLEED else CyclePhase.BLEED
            }
            // Pre-menstrual window: 3 days before bleed start
            val preStart = cycle.bleedStart.minusDays(3)
            if (!day.isBefore(preStart) && day.isBefore(cycle.bleedStart)) {
                return CyclePhase.PRE_MENSTRUAL
            }
        }
        return CyclePhase.NORMAL
    }

    private enum class CyclePhase { NORMAL, PRE_MENSTRUAL, EARLY_BLEED, BLEED }

    // ── Main generation ──────────────────────────────────────────────────

    suspend fun generate(months: Int) {
        require(months == 3 || months == 6) { "months must be 3 or 6" }

        // Clear all existing data (silent replace per FR-004a)
        multipleChoiceRepository.deleteAllSelections()
        multipleChoiceRepository.deleteAllOptions()
        dailyEntryRepository.deleteAll()
        insightRepository.deleteAllInsights()
        dataTypeRepository.deleteAll()

        // Create data types and collect IDs
        val createdTypes = mutableMapOf<String, DataTypeEntity>()
        val optionsMap = mutableMapOf<Long, List<MultipleChoiceOptionEntity>>()

        for (def in dataTypeDefs) {
            val entity = DataTypeEntity(
                emoji = def.emoji,
                description = def.description,
                inputType = def.inputType.name
            )
            val id = dataTypeRepository.insert(entity).getOrThrow()
            val saved = entity.copy(id = id)
            createdTypes[def.description] = saved

            if (def.options.isNotEmpty()) {
                val optionEntities = def.options.mapIndexed { index, (emoji, label) ->
                    MultipleChoiceOptionEntity(
                        dataTypeId = id,
                        emoji = emoji,
                        label = label,
                        sortOrder = index
                    )
                }
                multipleChoiceRepository.saveOptions(optionEntities)
                optionsMap[id] = multipleChoiceRepository.getOptions(id)
            }
        }

        val healthType = createdTypes["Health"]!!
        val medicationType = createdTypes["Medication"]!!
        val moodType = createdTypes["Mood"]!!
        val energyType = createdTypes["Energy Levels"]!!
        val exerciseType = createdTypes["Exercise"]!!
        val periodType = createdTypes["Period Bleeding"]!!

        val healthOptions = optionsMap[healthType.id]!!
        val exerciseOptions = optionsMap[exerciseType.id]!!

        val dizzyOption = healthOptions.first { it.label == "feeling dizzy" }
        val tirednessOption = healthOptions.first { it.label == "tiredness" }

        val today = LocalDate.now()
        val startDate = today.minusMonths(months.toLong())
        val totalDays = ChronoUnit.DAYS.between(startDate, today).toInt() + 1

        // Pre-compute menstrual cycles
        val cycles = precomputeCycles(startDate, today)

        // Pre-compute medication schedule (only streak-based misses, no random singles)
        val medicationSchedule = generateMedicationSchedule(startDate, today, months)

        // Pre-compute: for each day, how many days since the last missed streak ended
        // (used for post-streak dizzy carry-over)
        val daysSinceStreakEnd = computeDaysSinceStreakEnd(medicationSchedule)

        // Generate daily data
        val allEntries = mutableListOf<DailyEntryEntity>()
        val allSelections = mutableListOf<MultiChoiceSelectionEntity>()

        var day = startDate
        while (!day.isAfter(today)) {
            val dateStr = day.format(formatter)
            val dayIndex = ChronoUnit.DAYS.between(startDate, day).toInt()
            val progress = dayIndex.toDouble() / totalDays.coerceAtLeast(1)

            val cyclePhase = getCyclePhase(day, cycles)
            val medTaken = medicationSchedule[dayIndex]

            // Count consecutive missed medication days up to and including today
            val missedStreak = countMissedStreak(medicationSchedule, dayIndex)

            // Medication consistency: look back 60 days
            val medConsistency = computeMedConsistency(medicationSchedule, dayIndex)

            // ── Medication (TOGGLE) ──
            if (medTaken) {
                allEntries.add(DailyEntryEntity(date = dateStr, dataTypeId = medicationType.id))
            }

            // ── Period Bleeding (TOGGLE) ──
            val isBleeding = cyclePhase == CyclePhase.BLEED || cyclePhase == CyclePhase.EARLY_BLEED
            if (isBleeding) {
                allEntries.add(DailyEntryEntity(date = dateStr, dataTypeId = periodType.id))
            }

            // ── Mood (SCALE) ── FR-006, FR-008
            // Always generate mood entries for stronger correlations (95% of days)
            if (Random.nextFloat() < 0.95f) {
                // Base mood from medication consistency
                var moodBase = if (medConsistency) {
                    4.0 + gaussianRng.nextGaussian() * 0.5
                } else {
                    1.8 + gaussianRng.nextGaussian() * 0.6
                }

                // Direct per-day medication effect to strengthen point-biserial
                if (!medTaken && missedStreak >= 2) {
                    moodBase -= 0.5
                }

                // PMS and bleed penalty — applied to ALL bleed days and pre-menstrual
                when (cyclePhase) {
                    CyclePhase.PRE_MENSTRUAL -> moodBase -= 2.0 + Random.nextDouble() * 0.5
                    CyclePhase.EARLY_BLEED -> moodBase -= 3.0 + Random.nextDouble() * 0.5
                    CyclePhase.BLEED -> moodBase -= 2.0 + Random.nextDouble() * 0.5
                    else -> {}
                }
                val moodValue = moodBase.roundToInt().coerceIn(0, 5)
                allEntries.add(DailyEntryEntity(date = dateStr, dataTypeId = moodType.id, scaleValue = moodValue))
            }

            // ── Energy Levels (SCALE) ── FR-008
            // Always generate energy entries for stronger correlations (93% of days)
            if (Random.nextFloat() < 0.93f) {
                var energyBase = 3.2 + gaussianRng.nextGaussian() * 0.6

                // PMS and bleed penalty — applied to ALL bleed days and pre-menstrual
                when (cyclePhase) {
                    CyclePhase.PRE_MENSTRUAL -> energyBase -= 2.0 + Random.nextDouble() * 0.5
                    CyclePhase.EARLY_BLEED -> energyBase -= 3.0 + Random.nextDouble() * 0.5
                    CyclePhase.BLEED -> energyBase -= 2.0 + Random.nextDouble() * 0.5
                    else -> {}
                }
                val energyValue = energyBase.roundToInt().coerceIn(0, 5)
                allEntries.add(DailyEntryEntity(date = dateStr, dataTypeId = energyType.id, scaleValue = energyValue))
            }

            // ── Exercise (MULTIPLE_CHOICE) ──
            val exercised = Random.nextFloat() < 0.40f
            if (exercised) {
                allEntries.add(DailyEntryEntity(date = dateStr, dataTypeId = exerciseType.id))
                // Pick 1-2 random exercise options
                val numActivities = if (Random.nextFloat() < 0.3f) 2 else 1
                val selectedExOpts = exerciseOptions.shuffled().take(numActivities)
                for (opt in selectedExOpts) {
                    allSelections.add(MultiChoiceSelectionEntity(
                        date = dateStr, dataTypeId = exerciseType.id, optionId = opt.id
                    ))
                }
            }

            // ── Health (MULTIPLE_CHOICE) ── FR-005, FR-009, FR-010
            val healthSymptoms = mutableSetOf<MultipleChoiceOptionEntity>()

            // Medication–dizziness: missed 3+ days → very high P(dizzy) FR-005
            if (missedStreak >= 3) {
                // In an active missed streak of 3+ days: near-certain dizziness
                if (Random.nextFloat() < 0.95f) {
                    healthSymptoms.add(dizzyOption)
                }
            } else if (!medTaken && missedStreak >= 1) {
                // First 1-2 days of a new missed streak: moderately elevated
                if (Random.nextFloat() < 0.55f) {
                    healthSymptoms.add(dizzyOption)
                }
            } else if (daysSinceStreakEnd[dayIndex] in 1..2) {
                // 1-2 days after a missed streak ends: lingering dizziness
                if (Random.nextFloat() < 0.65f) {
                    healthSymptoms.add(dizzyOption)
                }
            } else {
                // Baseline dizzy probability (very low when medication is taken consistently)
                if (Random.nextFloat() < 0.01f) {
                    healthSymptoms.add(dizzyOption)
                }
            }

            // Exercise–tiredness with temporal weakening FR-009, FR-010
            val tirednessProb = if (exercised) {
                // Linear decay from 0.95 → 0.25 over the range
                0.95 - (0.70 * progress)
            } else {
                0.04 // very low baseline — maximises exercise contrast
            }
            if (Random.nextDouble() < tirednessProb) {
                healthSymptoms.add(tirednessOption)
            }

            // Random other health symptoms (natural variation)
            for (opt in healthOptions) {
                if (opt.id == dizzyOption.id || opt.id == tirednessOption.id) continue
                if (Random.nextFloat() < 0.06f) {
                    healthSymptoms.add(opt)
                }
            }

            if (healthSymptoms.isNotEmpty()) {
                allEntries.add(DailyEntryEntity(date = dateStr, dataTypeId = healthType.id))
                for (opt in healthSymptoms) {
                    allSelections.add(MultiChoiceSelectionEntity(
                        date = dateStr, dataTypeId = healthType.id, optionId = opt.id
                    ))
                }
            }

            day = day.plusDays(1)
        }

        // Batch insert entries grouped by date
        allEntries.groupBy { it.date }.forEach { (date, dayEntries) ->
            dailyEntryRepository.replaceEntriesForDate(date, dayEntries)
        }

        // Batch insert all MC selections grouped by date+dataType
        allSelections.groupBy { it.date to it.dataTypeId }.forEach { (key, sels) ->
            val (date, dtId) = key
            multipleChoiceRepository.saveSelections(date, dtId, sels.map { it.optionId }.toSet())
        }
    }

    // ── Medication schedule generation ── FR-005, FR-006
    // First ~2 months: streak-only misses (3-5 consecutive days) to produce clean PHI signal
    // After ~2 months: ~95% compliance (consistent use → mood improvement)
    private fun generateMedicationSchedule(startDate: LocalDate, endDate: LocalDate, months: Int): BooleanArray {
        val totalDays = ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
        val schedule = BooleanArray(totalDays) { true } // start all taken

        val transitionDay = 60 // ~2 months

        // Early period: only miss medication in streaks (no random single misses)
        // This creates a clean signal for PHI: "missed medication" almost always means a streak
        val earlyDays = minOf(transitionDay, totalDays)
        val numStreaks = if (months == 6) 6 else 3
        val streakStarts = mutableSetOf<Int>()
        repeat(numStreaks) {
            var attempts = 0
            while (attempts < 50) {
                val start = Random.nextInt(2, earlyDays - 6)
                // Don't overlap with existing streaks (need 10-day gap)
                if (streakStarts.none { kotlin.math.abs(it - start) < 10 }) {
                    streakStarts.add(start)
                    break
                }
                attempts++
            }
        }

        for (streakStart in streakStarts) {
            val streakLen = Random.nextInt(3, 6) // 3-5 days
            for (i in streakStart until minOf(streakStart + streakLen, earlyDays)) {
                schedule[i] = false
            }
        }

        // Late period: very high compliance (~97%) — only rare single misses
        for (i in transitionDay until totalDays) {
            if (Random.nextFloat() < 0.03f) {
                schedule[i] = false
            }
        }

        return schedule
    }

    /** For each day, compute how many days have passed since the end of the last missed streak (0 if currently in a streak or no streak ended recently). */
    private fun computeDaysSinceStreakEnd(schedule: BooleanArray): IntArray {
        val result = IntArray(schedule.size) { -1 } // -1 = no recent streak end
        var lastStreakEndDay = -100
        var wasInStreak = false

        for (i in schedule.indices) {
            if (!schedule[i]) {
                wasInStreak = true
            } else if (wasInStreak) {
                // Streak just ended yesterday — this is day 1 after streak
                lastStreakEndDay = i
                wasInStreak = false
            }
            if (schedule[i] && lastStreakEndDay >= 0) {
                result[i] = i - lastStreakEndDay + 1
            }
        }
        return result
    }

    private fun countMissedStreak(schedule: BooleanArray, dayIndex: Int): Int {
        var count = 0
        var i = dayIndex
        while (i >= 0 && !schedule[i]) {
            count++
            i--
        }
        return count
    }

    private fun computeMedConsistency(schedule: BooleanArray, dayIndex: Int): Boolean {
        val lookback = 60
        val start = maxOf(0, dayIndex - lookback + 1)
        if (dayIndex - start + 1 < 30) return false // not enough history
        val takenCount = (start..dayIndex).count { schedule[it] }
        val windowSize = dayIndex - start + 1
        return takenCount >= (windowSize * 0.88).toInt() // 88%+ compliance = consistent
    }
}
