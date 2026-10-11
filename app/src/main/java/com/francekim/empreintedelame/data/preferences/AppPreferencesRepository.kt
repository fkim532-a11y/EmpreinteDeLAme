package com.francekim.empreintedelame.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

class AppPreferencesRepository(private val context: Context) {

    private val introSeenKey = booleanPreferencesKey("intro_seen")
    private val activeProfileSlotKey = intPreferencesKey("active_profile_slot")
    private val previousActiveProfileSlotKey = intPreferencesKey("previous_active_profile_slot")

    val introSeen: Flow<Boolean> = context.dataStore.data.map { it[introSeenKey] ?: false }

    suspend fun setIntroSeen() {
        context.dataStore.edit { it[introSeenKey] = true }
    }

    val activeProfileSlot: Flow<Int> = context.dataStore.data.map { it[activeProfileSlotKey] ?: 1 }

    // Slot actif juste avant le dernier changement — utilisé pour y revenir
    // automatiquement si le profil actif est supprimé.
    val previousActiveProfileSlot: Flow<Int?> = context.dataStore.data.map { it[previousActiveProfileSlotKey] }

    suspend fun setActiveProfileSlot(slot: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[activeProfileSlotKey] ?: 1
            if (current != slot) prefs[previousActiveProfileSlotKey] = current
            prefs[activeProfileSlotKey] = slot
        }
    }

    private val appLanguageKey = stringPreferencesKey("app_language")

    /** "fr" / "en", ou null pour suivre la langue du téléphone. */
    val appLanguage: Flow<String?> = context.dataStore.data.map { it[appLanguageKey] }

    suspend fun setAppLanguage(language: String) {
        context.dataStore.edit { it[appLanguageKey] = language }
    }

    private val seenModuleIntrosKey = stringSetPreferencesKey("seen_module_intros")

    val seenModuleIntros: Flow<Set<String>> = context.dataStore.data.map { it[seenModuleIntrosKey] ?: emptySet() }

    suspend fun markModuleIntroSeen(moduleId: String) {
        context.dataStore.edit { prefs ->
            prefs[seenModuleIntrosKey] = (prefs[seenModuleIntrosKey] ?: emptySet()) + moduleId
        }
    }

    // ─── Tirage quotidien Miroir de l'Âme (par profil) ────────────────────────

    private fun mirrorEntryIndexKey(slot: Int)  = intPreferencesKey("mirror_soul_entry_index_$slot")
    private fun mirrorQueueKey(slot: Int)       = stringPreferencesKey("mirror_soul_queue_$slot")
    private fun mirrorLastDateKey(slot: Int)    = stringPreferencesKey("mirror_soul_last_date_$slot")
    private fun mirrorLastLifePathKey(slot: Int) = stringPreferencesKey("mirror_soul_last_life_path_$slot")

    fun mirrorSoulDrawState(slot: Int): Flow<MirrorSoulDrawState> = context.dataStore.data.map { prefs ->
        MirrorSoulDrawState(
            currentEntryIndex = prefs[mirrorEntryIndexKey(slot)],
            remainingQueue    = prefs[mirrorQueueKey(slot)]?.takeIf { it.isNotEmpty() }
                ?.split(",")?.map { it.toInt() } ?: emptyList(),
            lastDrawDate      = prefs[mirrorLastDateKey(slot)],
            lastLifePath      = prefs[mirrorLastLifePathKey(slot)]
        )
    }

    suspend fun saveMirrorSoulDrawState(slot: Int, state: MirrorSoulDrawState) {
        context.dataStore.edit { prefs ->
            state.currentEntryIndex?.let { prefs[mirrorEntryIndexKey(slot)] = it }
            prefs[mirrorQueueKey(slot)] = state.remainingQueue.joinToString(",")
            state.lastDrawDate?.let { prefs[mirrorLastDateKey(slot)] = it }
            state.lastLifePath?.let { prefs[mirrorLastLifePathKey(slot)] = it }
        }
    }
}

data class MirrorSoulDrawState(
    val currentEntryIndex : Int?,
    val remainingQueue    : List<Int>,
    val lastDrawDate      : String?,
    val lastLifePath      : String?
)
