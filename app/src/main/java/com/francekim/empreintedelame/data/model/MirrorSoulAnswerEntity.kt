package com.francekim.empreintedelame.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mirror_soul_answer")
data class MirrorSoulAnswerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileSlot     : Int,
    val timestampMillis : Long,
    val date            : String, // yyyy-MM-dd, une entrée au plus par profil et par jour
    val lifePathKey      : String,
    val texte           : String,
    val reponse         : String
)
