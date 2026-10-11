package com.francekim.empreintedelame.ui.hdpredictions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.francekim.empreintedelame.data.database.AppDatabase
import com.francekim.empreintedelame.data.model.HdPredictionsContent
import com.francekim.empreintedelame.data.model.HumanDesignGateInfo
import com.francekim.empreintedelame.data.preferences.AppPreferencesRepository
import com.francekim.empreintedelame.data.repository.HdPredictionsRepository
import com.francekim.empreintedelame.data.repository.HumanDesignRepository
import com.francekim.empreintedelame.data.repository.UserProfileRepository
import com.francekim.empreintedelame.domain.calculator.humandesign.HdDailyGate
import com.francekim.empreintedelame.domain.calculator.humandesign.HdDailyGateCalculator
import com.francekim.empreintedelame.domain.calculator.humandesign.HdLifeCycle
import com.francekim.empreintedelame.domain.calculator.humandesign.HdLifeCycleCalculator
import com.francekim.empreintedelame.domain.calculator.humandesign.HdPersonalMonthCalculator
import com.francekim.empreintedelame.domain.calculator.humandesign.HdPersonalYearCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

sealed class HdPredictionsUiState {
    object Loading : HdPredictionsUiState()
    object NoProfile : HdPredictionsUiState()
    data class Ready(
        val firstName      : String,
        val age            : Int,
        val lifeCycles     : List<HdLifeCycle>,
        val activeCycles   : List<HdLifeCycle>,
        val nextCycle      : HdLifeCycle?,
        val personalYear   : Int,
        val personalMonth  : Int,
        val dailyGate      : HdDailyGate,
        val content        : HdPredictionsContent,
        val gateInfo       : Map<Int, HumanDesignGateInfo>
    ) : HdPredictionsUiState()
}

class HdPredictionsViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = UserProfileRepository(
        AppDatabase.getInstance(app).userProfileDao(),
        AppPreferencesRepository(app)
    )

    private val contentRepo = HdPredictionsRepository(getApplication())
    private val content by lazy { contentRepo.load() }

    private val gateContentRepo = HumanDesignRepository(getApplication())
    private val gateInfo by lazy { gateContentRepo.load().gates }

    val uiState: StateFlow<HdPredictionsUiState> = repo.profile
        .map { userProfile ->
            if (userProfile == null) {
                HdPredictionsUiState.NoProfile
            } else {
                val today = LocalDate.now()
                val age = today.year - userProfile.birthYear
                val personalYear = HdPersonalYearCalculator.calculate(
                    birthDay    = userProfile.birthDay,
                    birthMonth  = userProfile.birthMonth,
                    currentYear = today.year
                )
                val personalMonth = HdPersonalMonthCalculator.calculate(
                    personalYear = personalYear,
                    currentMonth = today.monthValue
                )
                val lifeCycles = HdLifeCycleCalculator.calculateCycles(age)

                HdPredictionsUiState.Ready(
                    firstName     = userProfile.firstName,
                    age           = age,
                    lifeCycles    = lifeCycles,
                    activeCycles  = HdLifeCycleCalculator.activeCycles(age),
                    nextCycle     = HdLifeCycleCalculator.nextCycle(age),
                    personalYear  = personalYear,
                    personalMonth = personalMonth,
                    dailyGate     = HdDailyGateCalculator.personalGate(
                        birthDay     = userProfile.birthDay,
                        birthMonth   = userProfile.birthMonth,
                        currentDay   = today.dayOfMonth,
                        currentMonth = today.monthValue,
                        currentYear  = today.year
                    ),
                    content       = content,
                    gateInfo      = gateInfo
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HdPredictionsUiState.Loading)
}
