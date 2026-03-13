package com.martynamaron.biograph.data.repository

import com.martynamaron.biograph.data.local.UserPreferenceDao
import com.martynamaron.biograph.data.local.UserPreferenceEntity

class UserPreferenceRepository(private val dao: UserPreferenceDao) {

    companion object {
        private const val KEY_SORT_MODE = "insight_sort_mode"
    }

    suspend fun getSortMode(): String? = dao.getValue(KEY_SORT_MODE)

    suspend fun setSortMode(mode: String) {
        dao.upsert(UserPreferenceEntity(key = KEY_SORT_MODE, value = mode))
    }
}
