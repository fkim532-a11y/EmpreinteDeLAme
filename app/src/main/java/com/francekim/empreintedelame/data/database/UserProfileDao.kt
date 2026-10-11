package com.francekim.empreintedelame.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.francekim.empreintedelame.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(profile: UserProfile)

    @Query("SELECT * FROM user_profile WHERE slot = :slot")
    fun get(slot: Int): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile ORDER BY slot")
    fun getAll(): Flow<List<UserProfile>>

    @Query("DELETE FROM user_profile WHERE slot = :slot")
    suspend fun delete(slot: Int)

    @Query("SELECT slot FROM user_profile ORDER BY slot")
    suspend fun getAllSlotsOnce(): List<Int>
}
