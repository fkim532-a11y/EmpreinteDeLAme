package com.francekim.empreintedelame.ui.biorhythm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.francekim.empreintedelame.data.database.AppDatabase
import com.francekim.empreintedelame.data.model.BiorhythmContent
import com.francekim.empreintedelame.data.preferences.AppPreferencesRepository
import com.francekim.empreintedelame.data.repository.BiorhythmRepository
import com.francekim.empreintedelame.data.repository.UserProfileRepository
import com.francekim.empreintedelame.domain.calculator.BiorhythmResult
import com.francekim.empreintedelame.domain.calculator.BiorhythmCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed class BiorhythmUiState {
    object Loading : BiorhythmUiState()
    object NoProfile : BiorhythmUiState()
    data class Ready(
        val firstName : String,
        val result    : BiorhythmResult,
        val content   : BiorhythmContent
    ) : BiorhythmUiState()
}

class BiorhythmViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = UserProfileRepository(
        AppDatabase.getInstance(app).userProfileDao(),
        AppPreferencesRepository(app)
    )

    private val contentRepo = BiorhythmRepository(getApplication())
    private val content by lazy { contentRepo.load() }

    val uiState: StateFlow<BiorhythmUiState> = repo.profile
        .map { userProfile ->
            if (userProfile == null) {
                BiorhythmUiState.NoProfile
            } else {
                BiorhythmUiState.Ready(
                    firstName = userProfile.firstName,
                    result    = BiorhythmCalculator.calculate(
                        birthDay   = userProfile.birthDay,
                        birthMonth = userProfile.birthMonth,
                        birthYear  = userProfile.birthYear
                    ),
                    content   = content
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BiorhythmUiState.Loading)
}
