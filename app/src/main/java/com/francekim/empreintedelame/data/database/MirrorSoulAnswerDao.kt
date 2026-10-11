package com.francekim.empreintedelame.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.francekim.empreintedelame.data.model.MirrorSoulAnswerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MirrorSoulAnswerDao {
    @Insert
    suspend fun insert(answer: MirrorSoulAnswerEntity): Long

    @Query("SELECT * FROM mirror_soul_answer WHERE profileSlot = :slot ORDER BY timestampMillis DESC")
    fun getForSlot(slot: Int): Flow<List<MirrorSoulAnswerEntity>>

    @Query("SELECT * FROM mirror_soul_answer WHERE profileSlot = :slot AND date = :date LIMIT 1")
    suspend fun getForSlotAndDate(slot: Int, date: String): MirrorSoulAnswerEntity?

    @Query("UPDATE mirror_soul_answer SET reponse = :reponse WHERE id = :id")
    suspend fun updateAnswer(id: Long, reponse: String)
}
