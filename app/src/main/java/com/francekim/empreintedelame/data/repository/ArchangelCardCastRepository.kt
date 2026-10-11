package com.francekim.empreintedelame.data.repository

import com.francekim.empreintedelame.data.database.ArchangelCardCastDao
import com.francekim.empreintedelame.data.model.ArchangelCardCastEntity
import com.francekim.empreintedelame.data.preferences.AppPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

class ArchangelCardCastRepository(
    private val dao: ArchangelCardCastDao,
    private val prefsRepo: AppPreferencesRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val historyForActiveProfile: Flow<List<ArchangelCardCastEntity>> =
        prefsRepo.activeProfileSlot.flatMapLatest { slot -> dao.getForSlot(slot) }

    suspend fun save(slot: Int, cardId: Int, theme: String): Long =
        dao.insert(
            ArchangelCardCastEntity(
                profileSlot     = slot,
                timestampMillis = System.currentTimeMillis(),
                cardId          = cardId,
                theme           = theme
            )
        )

    suspend fun updateNote(id: Long, note: String) {
        dao.updateNote(id, note)
    }
}
