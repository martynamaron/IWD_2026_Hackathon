package com.martynamaron.biograph.data.local

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.martynamaron.biograph.data.DEFAULT_SUGGESTIONS
import com.martynamaron.biograph.data.InputType
import com.martynamaron.biograph.data.repository.DataTypeRepository
import com.martynamaron.biograph.data.repository.MultipleChoiceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        DataTypeEntity::class,
        DailyEntryEntity::class,
        MultipleChoiceOptionEntity::class,
        MultiChoiceSelectionEntity::class,
        InsightEntity::class,
        AnalysisMetadataEntity::class,
        UserPreferenceEntity::class
    ],
    version = 4,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4)
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dataTypeDao(): DataTypeDao
    abstract fun dailyEntryDao(): DailyEntryDao
    abstract fun multipleChoiceOptionDao(): MultipleChoiceOptionDao
    abstract fun multiChoiceSelectionDao(): MultiChoiceSelectionDao
    abstract fun insightDao(): InsightDao
    abstract fun analysisMetadataDao(): AnalysisMetadataDao
    abstract fun userPreferenceDao(): UserPreferenceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "biograph_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val instance = getInstance(context)
                                val dataTypeRepo = DataTypeRepository(instance.dataTypeDao())
                                val mcRepo = MultipleChoiceRepository(
                                    instance.multipleChoiceOptionDao(),
                                    instance.multiChoiceSelectionDao()
                                )

                                // Seed default data types from suggestions
                                DEFAULT_SUGGESTIONS.forEach { suggestion ->
                                    val entity = DataTypeEntity(
                                        emoji = suggestion.emoji,
                                        description = suggestion.description,
                                        inputType = suggestion.inputType.name
                                    )
                                    dataTypeRepo.insert(entity).getOrNull()?.let { id ->
                                        // Insert MC options for Multiple Choice types
                                        if (suggestion.inputType == InputType.MULTIPLE_CHOICE && suggestion.defaultOptions.isNotEmpty()) {
                                            mcRepo.saveOptions(
                                                suggestion.defaultOptions.mapIndexed { index, opt ->
                                                    MultipleChoiceOptionEntity(
                                                        dataTypeId = id,
                                                        emoji = opt.emoji,
                                                        label = opt.label,
                                                        sortOrder = index
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
