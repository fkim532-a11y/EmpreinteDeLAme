package com.francekim.empreintedelame.ui.archangelcard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.francekim.empreintedelame.data.database.AppDatabase
import com.francekim.empreintedelame.data.model.ArchangelCard
import com.francekim.empreintedelame.data.model.ArchangelCardCastEntity
import com.francekim.empreintedelame.data.preferences.AppPreferencesRepository
import com.francekim.empreintedelame.data.repository.ArchangelCardCastRepository
import com.francekim.empreintedelame.data.repository.ArchangelCardRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ArchangelCardHistoryItem(
    val entity: ArchangelCardCastEntity,
    val card: ArchangelCard?
)

class ArchangelCardHistoryViewModel(app: Application) : AndroidViewModel(app) {

    private val castRepo = ArchangelCardCastRepository(
        AppDatabase.getInstance(app).archangelCardCastDao(),
        AppPreferencesRepository(app)
    )
    private val deck: List<ArchangelCard> by lazy { ArchangelCardRepository(app).load() }

    val history: StateFlow<List<ArchangelCardHistoryItem>> = castRepo.historyForActiveProfile
        .map { list ->
            list.map { entity ->
                ArchangelCardHistoryItem(
                    entity = entity,
                    card   = deck.find { it.id == entity.cardId }
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateNote(id: Long, note: String) {
        viewModelScope.launch { castRepo.updateNote(id, note) }
    }
}
