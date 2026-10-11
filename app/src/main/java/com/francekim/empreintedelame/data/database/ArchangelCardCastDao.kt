package com.francekim.empreintedelame.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.francekim.empreintedelame.data.model.ArchangelCardCastEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArchangelCardCastDao {
    @Insert
    suspend fun insert(cast: ArchangelCardCastEntity): Long

    @Query("SELECT * FROM archangel_card_cast WHERE profileSlot = :slot ORDER BY timestampMillis DESC")
    fun getForSlot(slot: Int): Flow<List<ArchangelCardCastEntity>>

    @Query("UPDATE archangel_card_cast SET note = :note WHERE id = :id")
    suspend fun updateNote(id: Long, note: String)
}
