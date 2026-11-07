package com.driverprofit.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.driverprofit.data.AppDatabase
import com.driverprofit.data.DayEntry
import com.driverprofit.data.Settings
import com.driverprofit.data.SettingsStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).dayEntryDao()
    private val store = SettingsStore(app)

    val entries: StateFlow<List<DayEntry>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Null until the first read from DataStore completes. */
    val settings: StateFlow<Settings?> = store.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    suspend fun getEntry(id: Long): DayEntry? = dao.get(id)

    fun saveEntry(entry: DayEntry) {
        viewModelScope.launch { dao.upsert(entry) }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch { dao.delete(id) }
    }

    fun saveSettings(settings: Settings) {
        viewModelScope.launch { store.save(settings) }
    }
}
