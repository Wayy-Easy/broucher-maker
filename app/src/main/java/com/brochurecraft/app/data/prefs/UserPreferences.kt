package com.brochurecraft.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "brochurecraft_prefs")

class UserPreferences(private val context: Context) {

    private object Keys {
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val BUSINESS_TYPE = stringPreferencesKey("business_type")
        val BUSINESS_NAME = stringPreferencesKey("business_name")
        val IS_PRO = booleanPreferencesKey("is_pro")
        val USER_NAME = stringPreferencesKey("user_name")
    }

    val onboardingDone: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.ONBOARDING_DONE] ?: false }

    val businessType: Flow<String> =
        context.dataStore.data.map { it[Keys.BUSINESS_TYPE] ?: "Restaurant" }

    val businessName: Flow<String> =
        context.dataStore.data.map { it[Keys.BUSINESS_NAME] ?: "My Business" }

    val isPro: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.IS_PRO] ?: false }

    val userName: Flow<String> =
        context.dataStore.data.map { it[Keys.USER_NAME] ?: "Creator" }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_DONE] = done }
    }

    suspend fun setBusinessType(type: String) {
        context.dataStore.edit { it[Keys.BUSINESS_TYPE] = type }
    }

    suspend fun setBusinessName(name: String) {
        context.dataStore.edit { it[Keys.BUSINESS_NAME] = name }
    }

    suspend fun setPro(pro: Boolean) {
        context.dataStore.edit { it[Keys.IS_PRO] = pro }
    }
}
