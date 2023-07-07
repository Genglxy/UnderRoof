package com.genglxy.underroof.logic

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.genglxy.underroof.logic.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.util.UUID

class PreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    val storedQuery: Flow<String> = dataStore.data.map {
        it[SEARCH_QUERY_KEY] ?: ""
    }.distinctUntilChanged()

    suspend fun setStoredQuery(query: String) {
        dataStore.edit {
            it[SEARCH_QUERY_KEY] = query
        }
    }

    val storedMasterUUID: Flow<String> = dataStore.data.map {
        it[SEARCH_MASTER_UUID] ?: ""
    }.distinctUntilChanged()

    suspend fun setStoredMasterUUID(uuid: UUID) {
        Log.d("adfgh", "1 $uuid")
        dataStore.edit {
            Log.d("adfgh", "2 $uuid")
            it[SEARCH_MASTER_UUID] = uuid.toString()

            Log.d("adfgh", "3 $uuid")

        }
    }

    companion object {
        private val SEARCH_QUERY_KEY = stringPreferencesKey("search_query")
        private val SEARCH_MASTER_UUID = stringPreferencesKey("search_master_uuid")
        private var INSTANCE: PreferencesRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                val dataStore = PreferenceDataStoreFactory.create {
                    context.preferencesDataStoreFile("settings")
                }

                INSTANCE = PreferencesRepository(dataStore)
            }
        }

        fun get(): PreferencesRepository {
            return INSTANCE ?: throw IllegalStateException(
                "PreferencesRepository must be initialized"
            )
        }
    }
}