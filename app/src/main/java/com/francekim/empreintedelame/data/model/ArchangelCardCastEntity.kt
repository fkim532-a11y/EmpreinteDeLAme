package com.francekim.empreintedelame.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "archangel_card_cast")
data class ArchangelCardCastEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileSlot: Int,
    val timestampMillis: Long,
    val cardId: Int,
    val theme: String,
    val note: String? = null
)
