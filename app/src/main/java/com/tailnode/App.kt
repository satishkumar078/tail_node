package com.tailnode

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

class App: Application() {
    companion object {
        private const val PREFS_DB_NAME = "com.tailnode.prefs"
        lateinit var instance: App
    }

    val dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFS_DB_NAME)

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}