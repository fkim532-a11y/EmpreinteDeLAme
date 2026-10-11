package com.francekim.empreintedelame.ui.onboarding

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.francekim.empreintedelame.data.database.AppDatabase
import com.francekim.empreintedelame.data.model.UserProfile
import com.francekim.empreintedelame.data.preferences.AppPreferencesRepository
import com.francekim.empreintedelame.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OnboardingViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = UserProfileRepository(
        AppDatabase.getInstance(app).userProfileDao(),
        AppPreferencesRepository(app)
    )

    // Devient true dès que Room a répondu au moins une fois (que le profil soit null ou non).
    // Mis à jour dans la MÊME collecte que profile (onEach avant le stateIn) pour éviter
    // toute course entre deux souscriptions séparées au flux Room.
    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    // Profil du slot 1 : sert uniquement de garde pour l'Onboarding (premier lancement).
    // Les écrans de lecture (Destinée, Identité...) utilisent le profil ACTIF, pas celui-ci.
    val profile = repo.profileForSlot(1)
        .onEach { _isReady.value = true }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    // Les 3 profils, dans l'ordre des slots (1, 2, 3), pour l'écran "Profils".
    val allProfiles = repo.allProfiles
        .map { profiles -> (1..3).map { slot -> profiles.find { it.slot == slot } } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, listOf(null, null, null))

    val activeSlot = repo.activeSlot
        .stateIn(viewModelScope, SharingStarted.Eagerly, 1)

    suspend fun save(
        firstName: String,
        lastName: String,
        birthDay: Int,
        birthMonth: Int,
        birthYear: Int,
        birthHour: Int,
        birthMinute: Int,
        birthCity: String,
        birthUtcOffsetMinutes: Int,
        slot: Int = 1
    ) {
        repo.save(
            UserProfile(
                slot                   = slot,
                firstName              = firstName.trim(),
                lastName               = lastName.trim(),
                birthDay               = birthDay,
                birthMonth             = birthMonth,
                birthYear              = birthYear,
                birthHour              = birthHour,
                birthMinute            = birthMinute,
                birthCity              = birthCity.trim(),
                birthUtcOffsetMinutes  = birthUtcOffsetMinutes
            )
        )
    }

    fun setActiveSlot(slot: Int) {
        viewModelScope.launch { repo.setActiveSlot(slot) }
    }

    // Supprime un profil. S'il était actif, bascule sur le profil qui était
    // actif juste avant lui, ou à défaut sur le premier profil restant.
    fun deleteProfile(slot: Int) {
        viewModelScope.launch {
            val wasActive = activeSlot.value == slot
            val previous  = repo.previousActiveSlot.first()
            repo.deleteSlot(slot)
            if (wasActive) {
                val remaining = repo.remainingSlotsOnce()
                val fallback  = previous?.takeIf { it != slot && it in remaining } ?: remaining.firstOrNull()
                if (fallback != null) repo.setActiveSlot(fallback)
            }
        }
    }
}
