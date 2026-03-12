package com.martynamaron.biograph.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.martynamaron.biograph.data.DEFAULT_SUGGESTIONS
import com.martynamaron.biograph.data.repository.DailyEntryRepository
import com.martynamaron.biograph.data.repository.DataTypeRepository
import com.martynamaron.biograph.util.MockDataGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [DataTypeEntity::class, DailyEntryEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dataTypeDao(): DataTypeDao
    abstract fun dailyEntryDao(): DailyEntryDao

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

                                // Seed default data types from suggestions
                                val insertedTypes = DEFAULT_SUGGESTIONS.mapNotNull { suggestion ->
                                    val entity = DataTypeEntity(
                                        emoji = suggestion.emoji,
                                        description = suggestion.description
                                    )
                                    dataTypeRepo.insert(entity).getOrNull()?.let { id ->
                                        entity.copy(id = id)
                                    }
                                }

                                // Generate 2 months of mock data
                                MockDataGenerator(dataTypeRepo, dailyEntryRepo)
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
