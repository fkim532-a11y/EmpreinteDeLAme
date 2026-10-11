package com.francekim.empreintedelame.ui.compatibility

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.francekim.empreintedelame.data.database.AppDatabase
import com.francekim.empreintedelame.data.model.UserProfile
import com.francekim.empreintedelame.data.preferences.AppPreferencesRepository
import com.francekim.empreintedelame.data.repository.CompatibilityRepository
import com.francekim.empreintedelame.data.repository.UserProfileRepository
import com.francekim.empreintedelame.domain.calculator.CompatibilityCalculator
import com.francekim.empreintedelame.domain.calculator.CompatibilityResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CompatibilityUiState {
    object Loading : CompatibilityUiState()
    data class NeedProfiles(val availableCount: Int) : CompatibilityUiState()
    data class NeedSelection(val profiles: List<UserProfile>) : CompatibilityUiState()
    data class Ready(
        val profileA: UserProfile,
        val profileB: UserProfile,
        val result: CompatibilityResult,
        val canChangeSelection: Boolean
    ) : CompatibilityUiState()
}

class CompatibilityViewModel(app: Application) : AndroidViewModel(app) {

    private val profileRepo = UserProfileRepository(
        AppDatabase.getInstance(app).userProfileDao(),
        AppPreferencesRepository(app)
    )
    private val compatRepo = CompatibilityRepository(app)

    private var allProfiles: List<UserProfile> = emptyList()

    private val _uiState = MutableStateFlow<CompatibilityUiState>(CompatibilityUiState.Loading)
    val uiState: StateFlow<CompatibilityUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            profileRepo.allProfiles.collect { profiles ->
                allProfiles = profiles
                _uiState.value = when {
                    profiles.size < 2  -> CompatibilityUiState.NeedProfiles(profiles.size)
                    profiles.size == 2 -> readyState(profiles[0], profiles[1], canChange = false)
                    else                -> CompatibilityUiState.NeedSelection(profiles)
                }
            }
        }
    }

    private fun readyState(a: UserProfile, b: UserProfile, canChange: Boolean): CompatibilityUiState.Ready =
        CompatibilityUiState.Ready(
            profileA = a,
            profileB = b,
            result   = CompatibilityCalculator.compute(a, b, compatRepo),
            canChangeSelection = canChange
        )

    fun selectPair(a: UserProfile, b: UserProfile) {
        _uiState.value = readyState(a, b, canChange = true)
    }

    fun backToSelection() {
        if (allProfiles.size >= 2) {
            _uiState.value = CompatibilityUiState.NeedSelection(allProfiles)
        }
    }
}
