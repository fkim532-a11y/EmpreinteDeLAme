package com.francekim.empreintedelame.data.model

data class ArchangelCardTheme(
    val situation: String = "",
    val conseil: String = ""
)

data class ArchangelCard(
    val id: Int,
    val name: String,
    val keyword: String,
    val image: String,
    val themes: Map<String, ArchangelCardTheme>
) {
    fun themeFor(theme: String): ArchangelCardTheme =
        themes[theme] ?: ArchangelCardTheme()
}
