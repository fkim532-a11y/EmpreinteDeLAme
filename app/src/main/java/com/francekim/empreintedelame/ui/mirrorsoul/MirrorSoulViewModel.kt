package com.francekim.empreintedelame.ui.mirrorsoul

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.francekim.empreintedelame.data.database.AppDatabase
import com.francekim.empreintedelame.data.model.MirrorSoulEntry
import com.francekim.empreintedelame.data.preferences.AppPreferencesRepository
import com.francekim.empreintedelame.data.preferences.MirrorSoulDrawState
import com.francekim.empreintedelame.data.repository.MirrorSoulAnswerRepository
import com.francekim.empreintedelame.data.repository.MirrorSoulRepository
import com.francekim.empreintedelame.data.repository.UserProfileRepository
import com.francekim.empreintedelame.domain.calculator.MirrorSoulCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed class MirrorSoulUiState {
    object Loading : MirrorSoulUiState()
    object NoProfile : MirrorSoulUiState()
    data class Ready(
        val firstName     : String,
        val profileSlot   : Int,
        val lifePathKey   : String,
        val entry         : MirrorSoulEntry,
        val savedAnswer   : String?,
        val savedAnswerId : Long?
    ) : MirrorSoulUiState()
}

class MirrorSoulViewModel(app: Application) : AndroidViewModel(app) {

    private val prefsRepo = AppPreferencesRepository(app)
    private val profileRepo = UserProfileRepository(
        AppDatabase.getInstance(app).userProfileDao(),
        prefsRepo
    )
    private val answerRepo = MirrorSoulAnswerRepository(
        AppDatabase.getInstance(app).mirrorSoulAnswerDao(),
        prefsRepo
    )
    private val contentRepo = MirrorSoulRepository(getApplication())
    private val content by lazy { contentRepo.load() }

    private val _uiState = MutableStateFlow<MirrorSoulUiState>(MirrorSoulUiState.Loading)
    val uiState: StateFlow<MirrorSoulUiState> = _uiState.asStateFlow()

    val history = answerRepo.historyForActiveProfile

    init {
        viewModelScope.launch {
            profileRepo.activeSlot.collectLatest { slot ->
                val profile = profileRepo.profileForSlot(slot).first()
                if (profile == null) {
                    _uiState.value = MirrorSoulUiState.NoProfile
                    return@collectLatest
                }

                val lifePathKey = MirrorSoulCalculator.lifePathKey(profile.birthDay, profile.birthMonth, profile.birthYear)
                val pool = content.poolsByLifePath[lifePathKey]
                if (pool.isNullOrEmpty()) {
                    _uiState.value = MirrorSoulUiState.NoProfile
                    return@collectLatest
                }

                val today = LocalDate.now().toString()
                val savedState = prefsRepo.mirrorSoulDrawState(slot).first()
                val result = drawTodayEntry(pool.size, savedState, lifePathKey, today)
                if (result.newState != savedState) {
                    prefsRepo.saveMirrorSoulDrawState(slot, result.newState)
                }
                val entry = pool[result.entryIndex]
                val savedAnswer = if (entry.type == "question") answerRepo.getForSlotAndDate(slot, today) else null

                _uiState.value = MirrorSoulUiState.Ready(
                    firstName     = profile.firstName,
                    profileSlot   = slot,
                    lifePathKey   = lifePathKey,
                    entry         = entry,
                    savedAnswer   = savedAnswer?.reponse,
                    savedAnswerId = savedAnswer?.id
                )
            }
        }
    }

    fun saveAnswer(reponse: String) {
        val current = _uiState.value as? MirrorSoulUiState.Ready ?: return
        viewModelScope.launch {
            val today = LocalDate.now().toString()
            val id = current.savedAnswerId
            if (id != null) {
                answerRepo.updateAnswer(id, reponse)
                _uiState.value = current.copy(savedAnswer = reponse)
            } else {
                val newId = answerRepo.save(current.profileSlot, today, current.lifePathKey, current.entry.texte, reponse)
                _uiState.value = current.copy(savedAnswer = reponse, savedAnswerId = newId)
            }
        }
    }
}

private data class DrawResult(val entryIndex: Int, val newState: MirrorSoulDrawState)

// Tire l'entrée du jour sans répétition : réutilise l'entrée déjà tirée aujourd'hui si elle existe,
// sinon avance dans une file mélangée (remélangée dès qu'elle est épuisée ou que le chemin de vie change).
private fun drawTodayEntry(
    poolSize     : Int,
    currentState : MirrorSoulDrawState,
    lifePathKey  : String,
    today        : String
): DrawResult {
    val alreadyDrawnToday = currentState.lastDrawDate == today &&
        currentState.lastLifePath == lifePathKey &&
        currentState.currentEntryIndex != null

    if (alreadyDrawnToday) {
        return DrawResult(currentState.currentEntryIndex!!, currentState)
    }

    val needsReshuffle = currentState.lastLifePath != lifePathKey || currentState.remainingQueue.isEmpty()
    val queue = if (needsReshuffle) (0 until poolSize).shuffled() else currentState.remainingQueue
    val newIndex = queue.first()
    val newQueue = queue.drop(1)

    return DrawResult(
        entryIndex = newIndex,
        newState = MirrorSoulDrawState(
            currentEntryIndex = newIndex,
            remainingQueue    = newQueue,
            lastDrawDate      = today,
            lastLifePath      = lifePathKey
        )
    )
}
