package com.francekim.empreintedelame.domain.model

data class NumberInterpretation(
    val keyword    : String = "",
    // Destinée
    val lifePath   : String = "",
    val shadow     : String = "",
    val maturity   : String = "",
    val summit     : String = "",
    // Identité
    val expression : String = "",
    val soul       : String = "",
    val intimate   : String = "",
    val heritage   : String = "",
    val action     : String = "",
    val bridge     : String = "",
    // Portrait — inclusion
    val missing        : String = "",
    val overload       : String = "",
    val missingLifePath: String = "",
    val innate         : String = "",
    // Portrait — Nombre de l'Inconscient
    val unconscious : String = "",
    // Prédictif
    val year       : String = "",
    val month      : String = "",
    val day        : String = "",
    // Destinée — Année Personnelle
    val universalYear : String = "",
    val yearChallenge  : String = "",
    // Prédictif — Trimestres, Réalisation, Défi du mois
    val trimestre1     : String = "",
    val trimestre2     : String = "",
    val trimestre3     : String = "",
    val realization    : String = "",
    val monthChallenge : String = ""
)
