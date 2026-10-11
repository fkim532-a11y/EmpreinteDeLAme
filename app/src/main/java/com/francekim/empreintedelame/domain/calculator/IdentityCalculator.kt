package com.francekim.empreintedelame.domain.calculator

import kotlin.math.abs

data class IdentityProfile(
    val expressionNumber : Int,   // Nombre d'Expression — toutes les lettres
    val soulNumber       : Int,   // Élan Spirituel — voyelles (A E I O U Y)
    val intimateNumber   : Int,   // Nombre Intime — consonnes
    val heritageNumber   : Int,   // Nombre de l'Héritage — nom de famille seul
    val actionNumber     : Int,   // Nombre d'Action — premier prénom seul
    val bridgeNumber     : Int?   // Nombre Pont — null si chemin de vie non fourni
)

object IdentityCalculator {

    // Y est une voyelle en numérologie
    private val VOWELS = setOf('A', 'E', 'I', 'O', 'U', 'Y')

    fun calculate(
        firstName       : String,
        lastName        : String,
        lifePathReduced : Int? = null
    ): IdentityProfile {
        val fullNorm       = normalizeAccents("$firstName $lastName")
        val lastNorm       = normalizeAccents(lastName)
        // Premier prénom = premier mot avant espace ou tiret
        val firstFirstNorm = normalizeAccents(firstName.split(" ", "-").first())

        val expression = sumLetters(fullNorm)
        val soul       = sumLetters(fullNorm) { it in VOWELS }
        val intimate   = sumLetters(fullNorm) { it !in VOWELS }
        val heritage   = sumLetters(lastNorm)
        val action     = sumLetters(firstFirstNorm)
        // |Expression - Chemin de Vie|, réduit en préservant 11/22/33
        val bridge     = lifePathReduced?.let { lp -> reduce(abs(expression - lp)) }

        return IdentityProfile(
            expressionNumber = expression,
            soulNumber       = soul,
            intimateNumber   = intimate,
            heritageNumber   = heritage,
            actionNumber     = action,
            bridgeNumber     = bridge
        )
    }

    // Somme les valeurs Pythagorées des lettres filtrées, réduit (préserve 11/22/33).
    private fun sumLetters(
        normalized : String,
        filter     : (Char) -> Boolean = { true }
    ): Int {
        val total = normalized
            .filter { it.isLetter() && filter(it) }
            .mapNotNull { letterToDigit(it) }
            .sum()
        return reduce(total)
    }
}
