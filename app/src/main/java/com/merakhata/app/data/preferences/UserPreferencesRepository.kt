package com.merakhata.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        private val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val KEY_OWNER_NAME = stringPreferencesKey("owner_name")
        private val KEY_BUSINESS_NAME = stringPreferencesKey("business_name")
        private val KEY_CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        private val KEY_APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        private val KEY_PIN_HASH = stringPreferencesKey("pin_hash")
        private val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode") // "SYSTEM", "LIGHT", "DARK"
        private val KEY_UPDATE_URL = stringPreferencesKey("update_url")
        private val KEY_AUTO_CHECK_UPDATES = booleanPreferencesKey("auto_check_updates")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val ownerName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_OWNER_NAME] ?: ""
    }

    val businessName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_BUSINESS_NAME] ?: ""
    }

    val currencySymbol: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_CURRENCY_SYMBOL] ?: "₹"
    }

    val isAppLockEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_APP_LOCK_ENABLED] ?: false
    }

    val pinHash: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_PIN_HASH]
    }

    val isBiometricEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_BIOMETRIC_ENABLED] ?: false
    }

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME_MODE] ?: "LIGHT"
    }

    val updateUrl: Flow<String> = context.dataStore.data.map { preferences ->
        val saved = preferences[KEY_UPDATE_URL]
        if (saved.isNullOrEmpty() || saved.contains("loca.lt") || saved.contains("merakhata/app")) {
            "https://raw.githubusercontent.com/dk072/merakhata/main/update.json"
        } else {
            saved
        }
    }

    val autoCheckUpdates: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_AUTO_CHECK_UPDATES] ?: true
    }

    val userId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_ID]
    }

    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_EMAIL]
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_LOGGED_IN] ?: false
    }

    suspend fun setOnboardingCompleted(completed: Boolean, owner: String, business: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
            preferences[KEY_OWNER_NAME] = owner
            preferences[KEY_BUSINESS_NAME] = business
        }
    }

    suspend fun updateProfile(owner: String, business: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_OWNER_NAME] = owner
            preferences[KEY_BUSINESS_NAME] = business
        }
    }

    suspend fun setCloudUserLogin(id: String, email: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_ID] = id
            preferences[KEY_USER_EMAIL] = email
            preferences[KEY_IS_LOGGED_IN] = true
        }
    }

    suspend fun logoutCloudUser() {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_LOGGED_IN] = false
            preferences.remove(KEY_USER_ID)
            preferences.remove(KEY_USER_EMAIL)
        }
    }

    suspend fun setCurrencySymbol(symbol: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CURRENCY_SYMBOL] = symbol
        }
    }

    suspend fun setAppLock(enabled: Boolean, hash: String?) {
        context.dataStore.edit { preferences ->
            preferences[KEY_APP_LOCK_ENABLED] = enabled
            if (hash != null) {
                preferences[KEY_PIN_HASH] = hash
            } else if (!enabled) {
                preferences.remove(KEY_PIN_HASH)
            }
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode
        }
    }

    suspend fun setUpdateSettings(url: String, autoCheck: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_UPDATE_URL] = url
            preferences[KEY_AUTO_CHECK_UPDATES] = autoCheck
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
