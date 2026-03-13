package com.martynamaron.biograph.data.repository

import com.martynamaron.biograph.data.local.AnalysisMetadataDao
import com.martynamaron.biograph.data.local.AnalysisMetadataEntity
import com.martynamaron.biograph.data.local.DailyEntryDao
import com.martynamaron.biograph.data.local.InsightDao
import com.martynamaron.biograph.data.local.InsightEntity
import com.martynamaron.biograph.data.local.MultiChoiceSelectionDao
import kotlinx.coroutines.flow.Flow

class InsightRepository(
    private val insightDao: InsightDao,
    private val analysisMetadataDao: AnalysisMetadataDao,
    private val dailyEntryDao: DailyEntryDao,
    private val multiChoiceSelectionDao: MultiChoiceSelectionDao
) {
    fun getTopInsightsFlow(limit: Int = 10): Flow<List<InsightEntity>> =
        insightDao.getTopInsightsFlow(limit)

    suspend fun replaceAllInsights(insights: List<InsightEntity>) {
        insightDao.deleteAll()
        insightDao.insertAll(insights)
    }

    suspend fun deleteAllInsights() {
        insightDao.deleteAll()
    }

    suspend fun needsReanalysis(): Boolean {
        val meta = analysisMetadataDao.getMetadata()
        val currentCount = getCurrentDataCount()
        return meta == null || meta.lastDataCount != currentCount
    }

    suspend fun recordAnalysisCompleted() {
        val count = getCurrentDataCount()
        analysisMetadataDao.upsert(
            AnalysisMetadataEntity(
                id = 0,
                lastAnalysisTimestamp = System.currentTimeMillis(),
                lastDataCount = count
            )
        )
    }

    private suspend fun getCurrentDataCount(): Int {
        return dailyEntryDao.getTotalCount() + multiChoiceSelectionDao.getTotalCount()
    }
}
