package com.martynamaron.biograph.data.local

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.martynamaron.biograph.data.DEFAULT_SUGGESTIONS
import com.martynamaron.biograph.data.InputType
import com.martynamaron.biograph.data.repository.DailyEntryRepository
import com.martynamaron.biograph.data.repository.DataTypeRepository
import com.martynamaron.biograph.data.repository.MultipleChoiceRepository
import com.martynamaron.biograph.util.MockDataGenerator
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
        AnalysisMetadataEntity::class
    ],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3)
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dataTypeDao(): DataTypeDao
    abstract fun dailyEntryDao(): DailyEntryDao
    abstract fun multipleChoiceOptionDao(): MultipleChoiceOptionDao
    abstract fun multiChoiceSelectionDao(): MultiChoiceSelectionDao
    abstract fun insightDao(): InsightDao
    abstract fun analysisMetadataDao(): AnalysisMetadataDao

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
                                val dailyEntryRepo = DailyEntryRepository(instance.dailyEntryDao())
                                val mcRepo = MultipleChoiceRepository(
                                    instance.multipleChoiceOptionDao(),
                                    instance.multiChoiceSelectionDao()
                                )

                                // Seed default data types from suggestions
                                val insertedTypes = DEFAULT_SUGGESTIONS.mapNotNull { suggestion ->
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
                                        entity.copy(id = id)
                                    }
                                }

                                // Generate 2 months of mock data
                                MockDataGenerator(dataTypeRepo, dailyEntryRepo, mcRepo)
                                    .generate(insertedTypes)
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
