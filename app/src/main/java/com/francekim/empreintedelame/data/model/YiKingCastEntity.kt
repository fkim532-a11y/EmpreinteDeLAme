package com.francekim.empreintedelame.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "yi_king_cast")
data class YiKingCastEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileSlot: Int,
    val timestampMillis: Long,
    val theme: String,
    // Les 6 valeurs de traits (6/7/8/9), bas → haut, séparées par des virgules — ex. "7,8,9,6,7,8"
    val lineValues: String,
    val primaryNumero: Int,
    val secondaryNumero: Int?,
    val note: String? = null
)
