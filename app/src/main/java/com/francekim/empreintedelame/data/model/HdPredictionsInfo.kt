package com.francekim.empreintedelame.data.model

data class HdLifeCycleInfo(
    val name: String,
    val theme: String,
    val description: String,
    val invitation: String
)

data class HdPersonalYearInfo(
    val theme: String,
    val hdTheme: String,
    val description: String,
    val strategy: String,
    val affirmation: String
)

data class HdPersonalMonthInfo(
    val theme: String,
    val hdFocus: String,
    val description: String,
    val practicalTip: String
)

// Lecture "énergie du jour" d'une porte — distincte de HumanDesignGateInfo
// (qui décrit ce que signifie avoir cette porte active dans le thème de naissance).
data class HdGateReadingInfo(
    val yiKingName: String,
    val description: String,
    val invitation: String
)

data class HdPredictionsContent(
    val cycles: Map<String, HdLifeCycleInfo>,
    val personalYears: Map<Int, HdPersonalYearInfo>,
    val personalMonths: Map<Int, HdPersonalMonthInfo>,
    val dailyGates: Map<Int, HdGateReadingInfo>
)
