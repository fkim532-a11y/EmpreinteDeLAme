package com.francekim.empreintedelame.ui.yiking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.francekim.empreintedelame.data.database.AppDatabase
import com.francekim.empreintedelame.data.model.YiKingHexagram
import com.francekim.empreintedelame.data.preferences.AppPreferencesRepository
import com.francekim.empreintedelame.data.repository.UserProfileRepository
import com.francekim.empreintedelame.data.repository.YiKingCastRepository
import com.francekim.empreintedelame.data.repository.YiKingRepository
import com.francekim.empreintedelame.domain.calculator.YiKingCastCalculator
import com.francekim.empreintedelame.domain.calculator.YiKingCastResult
import com.francekim.empreintedelame.domain.calculator.YiKingLine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class YiKingUiState {
    object Loading : YiKingUiState()
    object NoProfile : YiKingUiState()
    data class ThemeSelect(val firstName: String) : YiKingUiState()
    data class Casting(
        val firstName: String,
        val theme: String,
        val lines: List<YiKingLine>
    ) : YiKingUiState()
    data class Result(
        val firstName: String,
        val theme: String,
        val cast: YiKingCastResult,
        val primary: YiKingHexagram?,
        val secondary: YiKingHexagram?,
        val castId: Long? = null,
        val note: String = ""
    ) : YiKingUiState()
}

class YiKingViewModel(app: Application) : AndroidViewModel(app) {

    private val profileRepo = UserProfileRepository(
        AppDatabase.getInstance(app).userProfileDao(),
        AppPreferencesRepository(app)
    )
    private val castRepo = YiKingCastRepository(
        AppDatabase.getInstance(app).yiKingCastDao(),
        AppPreferencesRepository(app)
    )
    private val content: Map<Int, YiKingHexagram> by lazy { YiKingRepository(app).load() }

    private var currentFirstName = ""
    private var currentSlot = 1

    private val _uiState = MutableStateFlow<YiKingUiState>(YiKingUiState.Loading)
    val uiState: StateFlow<YiKingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            profileRepo.profile.collect { profile ->
                if (profile == null) {
                    _uiState.value = YiKingUiState.NoProfile
                } else {
                    currentFirstName = profile.firstName
                    currentSlot = profile.slot
                    if (_uiState.value is YiKingUiState.Loading || _uiState.value is YiKingUiState.NoProfile) {
                        _uiState.value = YiKingUiState.ThemeSelect(profile.firstName)
                    }
                }
            }
        }
    }

    fun selectTheme(theme: String) {
        _uiState.value = YiKingUiState.Casting(currentFirstName, theme, emptyList())
    }

    // Détermine le résultat d'un lancer sans toucher à l'état — l'écran l'utilise pour
    // piloter l'animation des pièces avant de committer le trait via commitLine().
    fun prepareTossPreview(): YiKingLine =
        YiKingCastCalculator.tossLine()

    fun commitLine(line: YiKingLine) {
        val state = _uiState.value as? YiKingUiState.Casting ?: return
        if (state.lines.size >= 6) return

        val newLines = state.lines + line
        if (newLines.size == 6) {
            val result = YiKingCastCalculator.buildCast(newLines)
            _uiState.value = YiKingUiState.Result(
                firstName = state.firstName,
                theme     = state.theme,
                cast      = result,
                primary   = content[result.primaryNumero],
                secondary = result.secondaryNumero?.let { content[it] }
            )
            val slot = currentSlot
            val theme = state.theme
            viewModelScope.launch {
                val id = castRepo.save(slot, theme, result)
                (_uiState.value as? YiKingUiState.Result)?.let {
                    _uiState.value = it.copy(castId = id)
                }
            }
        } else {
            _uiState.value = state.copy(lines = newLines)
        }
    }

    fun updateNote(text: String) {
        val state = _uiState.value as? YiKingUiState.Result ?: return
        val id = state.castId ?: return
        _uiState.value = state.copy(note = text)
        viewModelScope.launch { castRepo.updateNote(id, text) }
    }

    fun newCast() {
        _uiState.value = YiKingUiState.ThemeSelect(currentFirstName)
    }
}
