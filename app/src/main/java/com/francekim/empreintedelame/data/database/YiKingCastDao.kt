package com.francekim.empreintedelame.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.francekim.empreintedelame.data.model.YiKingCastEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface YiKingCastDao {
    @Insert
    suspend fun insert(cast: YiKingCastEntity): Long

    @Query("SELECT * FROM yi_king_cast WHERE profileSlot = :slot ORDER BY timestampMillis DESC")
    fun getForSlot(slot: Int): Flow<List<YiKingCastEntity>>

    @Query("UPDATE yi_king_cast SET note = :note WHERE id = :id")
    suspend fun updateNote(id: Long, note: String)
}
