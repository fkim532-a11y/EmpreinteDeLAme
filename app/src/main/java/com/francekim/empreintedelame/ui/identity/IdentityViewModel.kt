package com.francekim.empreintedelame.ui.identity

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.francekim.empreintedelame.data.database.AppDatabase
import com.francekim.empreintedelame.data.preferences.AppPreferencesRepository
import com.francekim.empreintedelame.data.repository.InterpretationRepository
import com.francekim.empreintedelame.data.repository.UserProfileRepository
import com.francekim.empreintedelame.domain.calculator.DestinyCalculator
import com.francekim.empreintedelame.domain.calculator.IdentityCalculator
import com.francekim.empreintedelame.domain.calculator.IdentityProfile
import com.francekim.empreintedelame.domain.model.NumberInterpretation
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

sealed class IdentityUiState {
    object Loading   : IdentityUiState()
    object NoProfile : IdentityUiState()
    data class Ready(
        val firstName       : String,
        val profile         : IdentityProfile,
        val interpretations : Map<Int, NumberInterpretation>,
        val eightSpiritual  : NumberInterpretation?
    ) : IdentityUiState()
}

class IdentityViewModel(app: Application) : AndroidViewModel(app) {

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

    val uiState: StateFlow<IdentityUiState> = repo.profile
        .map { userProfile ->
            if (userProfile == null) IdentityUiState.NoProfile
            else {
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val destiny  = DestinyCalculator.calculate(userProfile, currentYear)
                val identity = IdentityCalculator.calculate(
                    firstName       = userProfile.firstName,
                    lastName        = userProfile.lastName,
                    lifePathReduced = destiny.lifePathReduced
                )
                IdentityUiState.Ready(userProfile.firstName, identity, interpretations, eightSpiritual)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), IdentityUiState.Loading)
}
