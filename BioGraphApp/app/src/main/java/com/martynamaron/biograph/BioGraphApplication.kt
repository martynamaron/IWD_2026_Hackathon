package com.martynamaron.biograph

import android.app.Application
import com.martynamaron.biograph.data.local.AppDatabase
import com.martynamaron.biograph.data.repository.DailyEntryRepository
import com.martynamaron.biograph.data.repository.DataTypeRepository

class BioGraphApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val dataTypeRepository: DataTypeRepository by lazy {
        DataTypeRepository(database.dataTypeDao())
    }

    val dailyEntryRepository: DailyEntryRepository by lazy {
        DailyEntryRepository(database.dailyEntryDao())
    }
}
