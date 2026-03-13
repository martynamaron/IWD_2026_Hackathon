package com.martynamaron.biograph.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InsightDao {
    @Query("""
        SELECT * FROM insights
        ORDER BY abs(correlationCoefficient) DESC
        LIMIT :limit
    """)
    fun getTopInsightsFlow(limit: Int = 10): Flow<List<InsightEntity>>

    @Query("SELECT * FROM insights ORDER BY abs(correlationCoefficient) DESC")
    suspend fun getAllInsights(): List<InsightEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(insights: List<InsightEntity>)

    @Query("DELETE FROM insights")
    suspend fun deleteAll()
}
