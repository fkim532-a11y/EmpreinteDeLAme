package com.francekim.empreintedelame.ui.humandesign

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.francekim.empreintedelame.data.database.AppDatabase
import com.francekim.empreintedelame.data.model.HumanDesignContent
import com.francekim.empreintedelame.data.preferences.AppPreferencesRepository
import com.francekim.empreintedelame.data.repository.HumanDesignRepository
import com.francekim.empreintedelame.data.repository.UserProfileRepository
import com.francekim.empreintedelame.domain.calculator.humandesign.HumanDesignCalculator
import com.francekim.empreintedelame.domain.calculator.humandesign.HumanDesignChart
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed class HumanDesignUiState {
    object Loading : HumanDesignUiState()
    object NoProfile : HumanDesignUiState()
    object BirthTimeUnknown : HumanDesignUiState()
    data class Ready(
        val firstName: String,
        val chart: HumanDesignChart,
        val content: HumanDesignContent
    ) : HumanDesignUiState()
}

class HumanDesignViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = UserProfileRepository(
        AppDatabase.getInstance(app).userProfileDao(),
        AppPreferencesRepository(app)
    )

    private val contentRepo = HumanDesignRepository(getApplication())
    private val content by lazy { contentRepo.load() }

    val uiState: StateFlow<HumanDesignUiState> = repo.profile
        .map { userProfile ->
            when {
                userProfile == null -> HumanDesignUiState.NoProfile
                else -> {
                    val chart = HumanDesignCalculator.calculate(userProfile)
                    if (chart == null) {
                        HumanDesignUiState.BirthTimeUnknown
                    } else {
                        HumanDesignUiState.Ready(firstName = userProfile.firstName, chart = chart, content = content)
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HumanDesignUiState.Loading)
}
