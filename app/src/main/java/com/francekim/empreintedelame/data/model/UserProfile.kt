package com.francekim.empreintedelame.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val slot: Int = 1,
    val firstName: String,
    val lastName: String,
    val birthDay: Int,
    val birthMonth: Int,
    val birthYear: Int,
    val birthHour: Int,
    val birthMinute: Int,
    val birthCity: String,
    val birthUtcOffsetMinutes: Int = 0
)
