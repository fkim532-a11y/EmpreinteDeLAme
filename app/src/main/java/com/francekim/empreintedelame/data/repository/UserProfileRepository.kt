package com.francekim.empreintedelame.data.repository

import com.francekim.empreintedelame.data.database.UserProfileDao
import com.francekim.empreintedelame.data.model.UserProfile
import com.francekim.empreintedelame.data.preferences.AppPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

class UserProfileRepository(
    private val dao: UserProfileDao,
    private val prefsRepo: AppPreferencesRepository
) {
    val activeSlot: Flow<Int> = prefsRepo.activeProfileSlot
    val previousActiveSlot: Flow<Int?> = prefsRepo.previousActiveProfileSlot

    @OptIn(ExperimentalCoroutinesApi::class)
    val profile: Flow<UserProfile?> = activeSlot.flatMapLatest { slot -> dao.get(slot) }

    val allProfiles: Flow<List<UserProfile>> = dao.getAll()

    fun profileForSlot(slot: Int): Flow<UserProfile?> = dao.get(slot)

    suspend fun save(profile: UserProfile) = dao.save(profile)

    suspend fun setActiveSlot(slot: Int) = prefsRepo.setActiveProfileSlot(slot)

    suspend fun deleteSlot(slot: Int) = dao.delete(slot)

    suspend fun remainingSlotsOnce(): List<Int> = dao.getAllSlotsOnce()
}
