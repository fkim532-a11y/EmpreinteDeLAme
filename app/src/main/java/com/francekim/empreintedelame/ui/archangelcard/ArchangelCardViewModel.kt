package com.francekim.empreintedelame.ui.archangelcard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.francekim.empreintedelame.data.database.AppDatabase
import com.francekim.empreintedelame.data.model.ArchangelCard
import com.francekim.empreintedelame.data.preferences.AppPreferencesRepository
import com.francekim.empreintedelame.data.repository.ArchangelCardCastRepository
import com.francekim.empreintedelame.data.repository.ArchangelCardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ArchangelCardUiState(
    val isLoading: Boolean = true,
    val card: ArchangelCard? = null,
    val selectedTheme: String? = null,
    val isFlipped: Boolean = false,
    val shuffleKey: Int = 0,
    val castId: Long? = null,
    val note: String = ""
)

class ArchangelCardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ArchangelCardRepository(application)
    private val prefsRepo = AppPreferencesRepository(application)
    private val castRepo = ArchangelCardCastRepository(
        AppDatabase.getInstance(application).archangelCardCastDao(),
        prefsRepo
    )
    private var deck: List<ArchangelCard> = emptyList()
    private var currentSlot = 1

    private val _uiState = MutableStateFlow(ArchangelCardUiState())
    val uiState: StateFlow<ArchangelCardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prefsRepo.activeProfileSlot.collect { currentSlot = it }
        }
        viewModelScope.launch {
            deck = repository.load()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    card = deck.randomOrNull(),
                    selectedTheme = null,
                    isFlipped = false
                )
            }
        }
    }

    fun selectTheme(theme: String) {
        _uiState.update { it.copy(selectedTheme = theme, isFlipped = true) }
        val cardId = _uiState.value.card?.id ?: return
        val slot = currentSlot
        viewModelScope.launch {
            val id = castRepo.save(slot, cardId, theme)
            _uiState.update { it.copy(castId = id, note = "") }
        }
    }

    fun updateNote(text: String) {
        val id = _uiState.value.castId ?: return
        _uiState.update { it.copy(note = text) }
        viewModelScope.launch { castRepo.updateNote(id, text) }
    }

    fun resetFlip() {
        if (deck.isEmpty()) return
        val currentId = _uiState.value.card?.id
        val newCard = deck.filter { it.id != currentId }.randomOrNull() ?: deck.random()
        _uiState.update {
            it.copy(
                card = newCard,
                selectedTheme = null,
                isFlipped = false,
                shuffleKey = it.shuffleKey + 1,
                castId = null,
                note = ""
            )
        }
    }
}
