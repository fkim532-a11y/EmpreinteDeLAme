package com.francekim.empreintedelame.ui.intro

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.francekim.empreintedelame.data.preferences.AppPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IntroViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AppPreferencesRepository(app)

    // Devient true dès que DataStore a répondu au moins une fois.
    // Mis à jour dans la MÊME collecte que introSeen (onEach avant le stateIn) pour éviter
    // toute course entre deux souscriptions séparées au même flux.
    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    val introSeen = repo.introSeen
        .onEach { _isReady.value = true }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun markIntroSeen() {
        viewModelScope.launch { repo.setIntroSeen() }
    }
}
