package com.francekim.empreintedelame.ui.predictive

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.francekim.empreintedelame.data.database.AppDatabase
import com.francekim.empreintedelame.data.preferences.AppPreferencesRepository
import com.francekim.empreintedelame.data.repository.InterpretationRepository
import com.francekim.empreintedelame.data.repository.UserProfileRepository
import com.francekim.empreintedelame.domain.calculator.PredictiveCalculator
import com.francekim.empreintedelame.domain.calculator.PredictiveProfile
import com.francekim.empreintedelame.domain.model.NumberInterpretation
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

sealed class PredictiveUiState {
    object Loading   : PredictiveUiState()
    object NoProfile : PredictiveUiState()
    data class Ready(
        val firstName        : String,
        val profile          : PredictiveProfile,
        val interpretations  : Map<Int, NumberInterpretation>,
        val letterTransits   : Map<Char, String>,
        val eightSpiritual   : NumberInterpretation?
    ) : PredictiveUiState()
}

class PredictiveViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = UserProfileRepository(
        AppDatabase.getInstance(app).userProfileDao(),
        AppPreferencesRepository(app)
    )

    private val interpretationRepo = InterpretationRepository(getApplication())

    private val interpretations: Map<Int, NumberInterpretation> by lazy {
        interpretationRepo.load()
    }

    private val letterTransits: Map<Char, String> by lazy {
        interpretationRepo.loadLetterTransits()
    }

    private val eightSpiritual: NumberInterpretation? by lazy {
        interpretationRepo.loadEightSpiritual()
    }

    val uiState: StateFlow<PredictiveUiState> = repo.profile
        .map { userProfile ->
            if (userProfile == null) PredictiveUiState.NoProfile
            else {
                val cal          = Calendar.getInstance()
                val currentYear  = cal.get(Calendar.YEAR)
                val currentMonth = cal.get(Calendar.MONTH) + 1
                val currentDay   = cal.get(Calendar.DAY_OF_MONTH)

                val predictive = PredictiveCalculator.calculate(
                    firstName    = userProfile.firstName,
                    birthDay     = userProfile.birthDay,
                    birthMonth   = userProfile.birthMonth,
                    birthYear    = userProfile.birthYear,
                    currentYear  = currentYear,
                    currentMonth = currentMonth,
                    currentDay   = currentDay
                )
                PredictiveUiState.Ready(
                    firstName       = userProfile.firstName,
                    profile         = predictive,
                    interpretations = interpretations,
                    letterTransits  = letterTransits,
                    eightSpiritual  = eightSpiritual
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PredictiveUiState.Loading)
}
