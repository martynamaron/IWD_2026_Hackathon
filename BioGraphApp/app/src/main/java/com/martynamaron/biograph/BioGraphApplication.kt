package com.martynamaron.biograph

import android.app.Application
import com.martynamaron.biograph.data.local.AppDatabase
import com.martynamaron.biograph.data.repository.DailyEntryRepository
import com.martynamaron.biograph.data.repository.DataTypeRepository
import com.martynamaron.biograph.data.repository.InsightRepository
import com.martynamaron.biograph.data.repository.MultipleChoiceRepository
import com.martynamaron.biograph.data.repository.UserPreferenceRepository

class BioGraphApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val dailyEntryRepository: DailyEntryRepository by lazy {
        DailyEntryRepository(database.dailyEntryDao())
    }

    val dataTypeRepository: DataTypeRepository by lazy {
        DataTypeRepository(database.dataTypeDao(), dailyEntryRepository)
    }

    val multipleChoiceRepository: MultipleChoiceRepository by lazy {
        MultipleChoiceRepository(
            database.multipleChoiceOptionDao(),
            database.multiChoiceSelectionDao(),
            database.dataTypeDao(),
            dailyEntryRepository
        )
    }

    val insightRepository: InsightRepository by lazy {
        InsightRepository(
            database.insightDao(),
            database.analysisMetadataDao(),
            database.dailyEntryDao(),
            database.multiChoiceSelectionDao()
        )
    }

    val userPreferenceRepository: UserPreferenceRepository by lazy {
        UserPreferenceRepository(database.userPreferenceDao())
    }
}
