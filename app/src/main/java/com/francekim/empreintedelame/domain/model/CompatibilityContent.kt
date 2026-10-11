package com.francekim.empreintedelame.domain.model

enum class CompatibilityNature { HARMONIEUX, COMPLEMENTAIRE, TENSION }

data class CompatibilityEntry(
    val score      : Int,
    val nature     : CompatibilityNature,
    val love       : String,
    val friendship : String,
    val family     : String,
    val work       : String,
    val chemistry  : String
)
