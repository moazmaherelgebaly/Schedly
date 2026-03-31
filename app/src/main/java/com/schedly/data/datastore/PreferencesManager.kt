package com.schedly.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.schedly.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "schedly_preferences")

class PreferencesManager(private val dataStore: DataStore<Preferences>) : PreferencesRepository {

    companion object {
        val RAMADAN_OFFSET_KEY = intPreferencesKey("ramadan_offset")
        val LAST_VIEW_MODE_KEY = stringPreferencesKey("last_view_mode")
        val SORT_PREFERENCES_KEY = stringPreferencesKey("sort_preferences")
    }

    fun getRamadanOffsetFlow(): Flow<Int> {
        return dataStore.data.map { preferences ->
            preferences[RAMADAN_OFFSET_KEY] ?: 0
        }
    }

    override suspend fun getRamadanOffset(): Int {
        return dataStore.data.map { preferences ->
            preferences[RAMADAN_OFFSET_KEY] ?: 0
        }.firstOrNull() ?: 0
    }

    override suspend fun setRamadanOffset(offset: Int) {
        dataStore.edit { preferences ->
            preferences[RAMADAN_OFFSET_KEY] = offset
        }
    }

    fun getLastViewModeFlow(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[LAST_VIEW_MODE_KEY] ?: "VERTICAL"
        }
    }

    suspend fun setLastViewMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[LAST_VIEW_MODE_KEY] = mode
        }
    }
}
