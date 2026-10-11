package com.francekim.empreintedelame.data.model

data class MirrorSoulEntry(
    val type  : String, // "question" ou "energie"
    val texte : String
)

data class MirrorSoulContent(
    val poolsByLifePath: Map<String, List<MirrorSoulEntry>>
)
