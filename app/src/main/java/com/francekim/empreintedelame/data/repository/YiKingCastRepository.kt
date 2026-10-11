package com.francekim.empreintedelame.data.repository

import com.francekim.empreintedelame.data.database.YiKingCastDao
import com.francekim.empreintedelame.data.model.YiKingCastEntity
import com.francekim.empreintedelame.data.preferences.AppPreferencesRepository
import com.francekim.empreintedelame.domain.calculator.YiKingCastResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

class YiKingCastRepository(
    private val dao: YiKingCastDao,
    private val prefsRepo: AppPreferencesRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val historyForActiveProfile: Flow<List<YiKingCastEntity>> =
        prefsRepo.activeProfileSlot.flatMapLatest { slot -> dao.getForSlot(slot) }

    suspend fun save(slot: Int, theme: String, result: YiKingCastResult): Long =
        dao.insert(
            YiKingCastEntity(
                profileSlot      = slot,
                timestampMillis  = System.currentTimeMillis(),
                theme            = theme,
                lineValues       = result.lines.joinToString(",") { it.value.toString() },
                primaryNumero    = result.primaryNumero,
                secondaryNumero  = result.secondaryNumero
            )
        )

    suspend fun updateNote(id: Long, note: String) {
        dao.updateNote(id, note)
    }
}
