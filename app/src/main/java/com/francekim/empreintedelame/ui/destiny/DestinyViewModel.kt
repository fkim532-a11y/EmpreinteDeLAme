package com.francekim.empreintedelame.ui.destiny

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.francekim.empreintedelame.data.database.AppDatabase
import com.francekim.empreintedelame.data.preferences.AppPreferencesRepository
import com.francekim.empreintedelame.data.repository.InterpretationRepository
import com.francekim.empreintedelame.data.repository.UserProfileRepository
import com.francekim.empreintedelame.domain.calculator.DestinyCalculator
import com.francekim.empreintedelame.domain.calculator.DestinyProfile
import com.francekim.empreintedelame.domain.model.NumberInterpretation
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

sealed class DestinyUiState {
    object Loading   : DestinyUiState()
    object NoProfile : DestinyUiState()
    data class Ready(
        val firstName       : String,
        val profile         : DestinyProfile,
        val interpretations : Map<Int, NumberInterpretation>,
        val eightSpiritual  : NumberInterpretation?
    ) : DestinyUiState()
}

class DestinyViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = UserProfileRepository(
        AppDatabase.getInstance(app).userProfileDao(),
        AppPreferencesRepository(app)
    )

    private val interpretationRepo = InterpretationRepository(getApplication())

    private val interpretations: Map<Int, NumberInterpretation> by lazy {
        interpretationRepo.load()
    }

    private val eightSpiritual: NumberInterpretation? by lazy {
        interpretationRepo.loadEightSpiritual()
    }

    val uiState: StateFlow<DestinyUiState> = repo.profile
        .map { userProfile ->
            if (userProfile == null) DestinyUiState.NoProfile
            else DestinyUiState.Ready(
                firstName = userProfile.firstName,
                profile = DestinyCalculator.calculate(
                    profile     = userProfile,
                    currentYear = Calendar.getInstance().get(Calendar.YEAR)
                ),
                interpretations = interpretations,
                eightSpiritual  = eightSpiritual
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DestinyUiState.Loading)
}
