package com.francekim.empreintedelame.data.model

data class YiKingThemes(
    val amour: String = "",
    val finance: String = "",
    val sante: String = "",
    val spirituel: String = "",
    val famille: String = ""
) {
    fun forKey(key: String): String = when (key) {
        "amour"     -> amour
        "finance"   -> finance
        "sante"     -> sante
        "spirituel" -> spirituel
        "famille"   -> famille
        else        -> ""
    }
}

data class YiKingHexagram(
    val numero: Int,
    val nom: String,
    val nomChinois: String,
    val trigrammeHaut: String,
    val trigrammeBas: String,
    val image: String,
    val jugementGeneral: String,
    val themes: YiKingThemes,
    val traits: Map<Int, String>
)
