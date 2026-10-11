package com.francekim.empreintedelame.ui.moduleintro

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

class ModuleIntroViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = AppPreferencesRepository(app)

    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    val seenModuleIntros = repo.seenModuleIntros
        .onEach { _isReady.value = true }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptySet())

    fun markSeen(moduleId: String) {
        viewModelScope.launch { repo.markModuleIntroSeen(moduleId) }
    }
}
