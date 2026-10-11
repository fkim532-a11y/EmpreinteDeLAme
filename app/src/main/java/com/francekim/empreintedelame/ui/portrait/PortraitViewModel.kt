package com.francekim.empreintedelame.ui.portrait

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.francekim.empreintedelame.data.database.AppDatabase
import com.francekim.empreintedelame.data.preferences.AppPreferencesRepository
import com.francekim.empreintedelame.data.repository.InterpretationRepository
import com.francekim.empreintedelame.data.repository.UserProfileRepository
import com.francekim.empreintedelame.domain.calculator.Arrow
import com.francekim.empreintedelame.domain.calculator.PortraitCalculator
import com.francekim.empreintedelame.domain.calculator.PortraitProfile
import com.francekim.empreintedelame.domain.model.ArrowInterpretation
import com.francekim.empreintedelame.domain.model.NumberInterpretation
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed class PortraitUiState {
    object Loading   : PortraitUiState()
    object NoProfile : PortraitUiState()
    data class Ready(
        val firstName       : String,
        val profile         : PortraitProfile,
        val interpretations : Map<Int, NumberInterpretation>,
        val eightSpiritual  : NumberInterpretation?,
        val arrows          : Map<Arrow, ArrowInterpretation>
    ) : PortraitUiState()
}

class PortraitViewModel(app: Application) : AndroidViewModel(app) {

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

    private val arrows: Map<Arrow, ArrowInterpretation> by lazy {
        interpretationRepo.loadArrows()
    }

    val uiState: StateFlow<PortraitUiState> = repo.profile
        .map { userProfile ->
            if (userProfile == null) PortraitUiState.NoProfile
            else PortraitUiState.Ready(
                firstName = userProfile.firstName,
                profile = PortraitCalculator.calculate(
                    firstName  = userProfile.firstName,
                    lastName   = userProfile.lastName,
                    birthDay   = userProfile.birthDay,
                    birthMonth = userProfile.birthMonth,
                    birthYear  = userProfile.birthYear
                ),
                interpretations = interpretations,
                eightSpiritual  = eightSpiritual,
                arrows          = arrows
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PortraitUiState.Loading)
}
