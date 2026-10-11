package com.francekim.empreintedelame.ui.yiking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.francekim.empreintedelame.data.database.AppDatabase
import com.francekim.empreintedelame.data.model.YiKingCastEntity
import com.francekim.empreintedelame.data.model.YiKingHexagram
import com.francekim.empreintedelame.data.preferences.AppPreferencesRepository
import com.francekim.empreintedelame.data.repository.UserProfileRepository
import com.francekim.empreintedelame.data.repository.YiKingCastRepository
import com.francekim.empreintedelame.data.repository.YiKingRepository
import com.francekim.empreintedelame.domain.calculator.YiKingCastResult
import com.francekim.empreintedelame.domain.calculator.YiKingLine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class YiKingHistoryItem(
    val entity: YiKingCastEntity,
    val primary: YiKingHexagram?,
    val secondary: YiKingHexagram?
) {
    val cast: YiKingCastResult get() = entity.toCastResult()
}

fun YiKingCastEntity.toCastResult(): YiKingCastResult {
    val lines = lineValues.split(",").map { raw -> YiKingLine.fromValue(raw.trim().toInt()) }
    val pattern = lines.joinToString("") { if (it.isYang) "1" else "0" }
    return YiKingCastResult(lines, pattern, primaryNumero, secondaryNumero)
}

class YiKingHistoryViewModel(app: Application) : AndroidViewModel(app) {

    private val castRepo = YiKingCastRepository(
        AppDatabase.getInstance(app).yiKingCastDao(),
        AppPreferencesRepository(app)
    )
    private val profileRepo = UserProfileRepository(
        AppDatabase.getInstance(app).userProfileDao(),
        AppPreferencesRepository(app)
    )
    private val content: Map<Int, YiKingHexagram> by lazy { YiKingRepository(app).load() }

    // Sert uniquement à titrer le partage d'une entrée du journal ("✨ Yi King de {prénom}").
    val firstName: StateFlow<String> = profileRepo.profile
        .map { it?.firstName ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val history: StateFlow<List<YiKingHistoryItem>> = castRepo.historyForActiveProfile
        .map { list ->
            list.map { entity ->
                YiKingHistoryItem(
                    entity    = entity,
                    primary   = content[entity.primaryNumero],
                    secondary = entity.secondaryNumero?.let { content[it] }
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateNote(id: Long, note: String) {
        viewModelScope.launch { castRepo.updateNote(id, note) }
    }
}
