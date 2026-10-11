package com.francekim.empreintedelame.ui.archangel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.francekim.empreintedelame.data.database.AppDatabase
import com.francekim.empreintedelame.data.preferences.AppPreferencesRepository
import com.francekim.empreintedelame.data.repository.ArchangelRepository
import com.francekim.empreintedelame.data.repository.UserProfileRepository
import com.francekim.empreintedelame.domain.calculator.Archangel
import com.francekim.empreintedelame.domain.calculator.ArchangelCalculator
import com.francekim.empreintedelame.domain.model.ArchangelInfo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed class ArchangelUiState {
    object Loading   : ArchangelUiState()
    object NoProfile : ArchangelUiState()
    data class Ready(
        val firstName : String,
        val archangel : Archangel,
        val info      : ArchangelInfo?
    ) : ArchangelUiState()
}

class ArchangelViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = UserProfileRepository(
        AppDatabase.getInstance(app).userProfileDao(),
        AppPreferencesRepository(app)
    )

    private val archangelRepo = ArchangelRepository(getApplication())

    private val archangels: Map<Archangel, ArchangelInfo> by lazy {
        archangelRepo.load()
    }

    val uiState: StateFlow<ArchangelUiState> = repo.profile
        .map { userProfile ->
            if (userProfile == null) {
                ArchangelUiState.NoProfile
            } else {
                val archangel = ArchangelCalculator.calculate(userProfile)
                ArchangelUiState.Ready(
                    firstName = userProfile.firstName,
                    archangel = archangel,
                    info      = archangels[archangel]
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ArchangelUiState.Loading)
}
