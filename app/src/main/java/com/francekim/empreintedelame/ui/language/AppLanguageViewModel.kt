package com.francekim.empreintedelame.ui.language

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.francekim.empreintedelame.data.preferences.AppPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppLanguageViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AppPreferencesRepository(application)

    /** "fr" / "en", ou null tant que la préférence suit la langue du téléphone. */
    val language: StateFlow<String?> = repo.appLanguage.stateIn(
        scope   = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    fun setLanguage(language: String) {
        viewModelScope.launch { repo.setAppLanguage(language) }
    }
}
