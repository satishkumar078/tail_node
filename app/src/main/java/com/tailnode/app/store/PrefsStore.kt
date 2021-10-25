package com.tailnode.app.store

import androidx.datastore.preferences.core.*
import androidx.lifecycle.asLiveData
import com.tailnode.App
import kotlinx.coroutines.flow.*
import java.io.IOException

object PrefsStore {

    fun appState() = read(PreferencesKeys.APP_STATE_KEY, AppState.LOGIN.name)
        .map {
            return@map AppState.valueOf(it)
        }.asLiveData()

    suspend fun setAppState(state: AppState) {
        save(PreferencesKeys.APP_STATE_KEY, state.name)
    }

    fun getUserName() = read(PreferencesKeys.USER_NAME, "")
        .map {
            return@map it
        }.asLiveData()

    suspend fun setUserName(name: String) {
        save(PreferencesKeys.USER_NAME, name)
    }

    fun getUserMobile() = read(PreferencesKeys.USER_MOBILE, "")
        .map {
            return@map it
        }.asLiveData()

    suspend fun setUserMobile(mobile: String) {
        save(PreferencesKeys.USER_MOBILE, mobile)
    }

    suspend fun reset() {
        App.instance.dataStore.edit {
            it.clear()
        }
    }

    //internal
    private fun <T>read(key: Preferences.Key<T>, defVal: T) =
        App.instance.dataStore.data.catch { exceptions ->
            if (exceptions is IOException) {
                emit(emptyPreferences())
            } else throw exceptions
        }.map { return@map it[key] ?: defVal }

    private suspend fun <T>save(key: Preferences.Key<T>, value: T) {
        App.instance.dataStore.edit {
            it[key] = value
        }
    }

    private object PreferencesKeys {
        val APP_STATE_KEY = stringPreferencesKey("APP_STATE_KEY")
        val USER_NAME = stringPreferencesKey("USER_NAME")
        val USER_MOBILE = stringPreferencesKey("USER_MOBILE")
    }
}