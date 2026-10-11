package com.francekim.empreintedelame.data.repository

import com.francekim.empreintedelame.data.database.MirrorSoulAnswerDao
import com.francekim.empreintedelame.data.model.MirrorSoulAnswerEntity
import com.francekim.empreintedelame.data.preferences.AppPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

class MirrorSoulAnswerRepository(
    private val dao: MirrorSoulAnswerDao,
    private val prefsRepo: AppPreferencesRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val historyForActiveProfile: Flow<List<MirrorSoulAnswerEntity>> =
        prefsRepo.activeProfileSlot.flatMapLatest { slot -> dao.getForSlot(slot) }

    suspend fun getForSlotAndDate(slot: Int, date: String): MirrorSoulAnswerEntity? =
        dao.getForSlotAndDate(slot, date)

    suspend fun save(slot: Int, date: String, lifePathKey: String, texte: String, reponse: String): Long =
        dao.insert(
            MirrorSoulAnswerEntity(
                profileSlot     = slot,
                timestampMillis = System.currentTimeMillis(),
                date            = date,
                lifePathKey     = lifePathKey,
                texte           = texte,
                reponse         = reponse
            )
        )

    suspend fun updateAnswer(id: Long, reponse: String) {
        dao.updateAnswer(id, reponse)
    }
}
