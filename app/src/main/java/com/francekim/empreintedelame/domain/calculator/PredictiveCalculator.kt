package com.francekim.empreintedelame.domain.calculator

import kotlin.math.abs

data class LetterTransit(
    val letter        : Char,
    val letterValue   : Int,   // valeur Pythagorée de la lettre
    val yearInTransit : Int    // 1 = première année de ce transit, 2 = deuxième, etc.
)

data class PredictiveProfile(
    // Temps annuel
    val personalYear      : Int,
    val trimestre1        : Int,
    val trimestre2        : Int,
    val trimestre3        : Int,
    val currentTrimestre  : Int,   // 1, 2 ou 3

    // Temps mensuel
    val personalMonth      : Int,
    val monthlyRealization : Int,  // Mois Personnel + Mois Universel
    val monthChallenge     : Int,  // |Jour de naissance réduit − Mois actuel réduit|

    // Temps quotidien
    val personalDay : Int,

    // Transit de la lettre (premier prénom)
    val letterTransit : LetterTransit,

    // Heures miroirs résonnant avec le Jour Personnel
    val favoriteMirrorHours : List<Int>
)

object PredictiveCalculator {

    fun calculate(
        firstName    : String,
        birthDay     : Int,
        birthMonth   : Int,
        birthYear    : Int,
        currentYear  : Int,
        currentMonth : Int,
        currentDay   : Int
    ): PredictiveProfile {

        // ── Composants de base ────────────────────────────────────────────────
        val dayR   = reducePredictive(birthDay)
        val monthR = reducePredictive(birthMonth)
        val yearR  = reducePredictive(currentYear.toString().sumOf { it.digitToInt() })

        // ── Année Personnelle ──────────────────────────────────────────────────
        val personalYear = reducePredictive(dayR + monthR + yearR)

        // ── Les 3 Trimestres ───────────────────────────────────────────────────
        val trimestre1 = reducePredictive(personalYear + monthR)
        val trimestre2 = reducePredictive(trimestre1   + dayR)
        val trimestre3 = reducePredictive(trimestre2   + yearR)
        val currentTrimestre = when (currentMonth) {
            in 1..4 -> 1
            in 5..8 -> 2
            else    -> 3
        }

        // ── Mois Personnel ─────────────────────────────────────────────────────
        val curMonthR     = reducePredictive(currentMonth)
        val personalMonth = reducePredictive(personalYear + curMonthR)

        // ── Réalisation Mensuelle ──────────────────────────────────────────────
        val universalMonth     = reducePredictive(currentMonth + yearR)
        val monthlyRealization = reducePredictive(personalMonth + universalMonth)

        // ── Défi du Mois ───────────────────────────────────────────────────────
        val monthChallenge = reducePredictive(abs(dayR - curMonthR))

        // ── Jour Personnel ─────────────────────────────────────────────────────
        val curDayR     = reducePredictive(currentDay)
        val personalDay = reducePredictive(personalMonth + curDayR)

        // ── Transit de la Lettre ───────────────────────────────────────────────
        val letterTransit = calculateLetterTransit(firstName, currentYear, birthYear)

        // ── Heures Miroirs ─────────────────────────────────────────────────────
        // Pour les nombres maîtres, on compare avec leur réduction de base
        val matchValue = when (personalDay) {
            11 -> 2
            22 -> 4
            else -> personalDay
        }
        val favoriteMirrorHours = (0..23).filter { h ->
            val s = h.toString().sumOf { it.digitToInt() }
            s > 0 && simpleReduce(s) == matchValue
        }

        return PredictiveProfile(
            personalYear        = personalYear,
            trimestre1          = trimestre1,
            trimestre2          = trimestre2,
            trimestre3          = trimestre3,
            currentTrimestre    = currentTrimestre,
            personalMonth       = personalMonth,
            monthlyRealization  = monthlyRealization,
            monthChallenge      = monthChallenge,
            personalDay         = personalDay,
            letterTransit       = letterTransit,
            favoriteMirrorHours = favoriteMirrorHours
        )
    }

    private fun calculateLetterTransit(
        firstName   : String,
        currentYear : Int,
        birthYear   : Int
    ): LetterTransit {
        val firstWord = firstName.split(" ", "-").first()
        val letters   = normalizeAccents(firstWord).filter { it.isLetter() }
        val values    = letters.mapNotNull { letterToDigit(it) }

        val cycleDuration = values.sum().takeIf { it > 0 }
            ?: return LetterTransit('?', 0, 1)

        val currentAge = currentYear - birthYear
        val posInCycle = currentAge % cycleDuration

        var cumulative = 0
        for (i in values.indices) {
            val nextCumulative = cumulative + values[i]
            if (posInCycle < nextCumulative) {
                return LetterTransit(
                    letter        = letters[i],
                    letterValue   = values[i],
                    yearInTransit = posInCycle - cumulative + 1
                )
            }
            cumulative = nextCumulative
        }
        // Fallback (ne devrait jamais arriver)
        return LetterTransit(letters[0], values[0], 1)
    }

    // Réduction simple 1-9 sans préservation des maîtres (pour les heures).
    private fun simpleReduce(n: Int): Int {
        var v = n
        while (v > 9) v = v.toString().sumOf { it.digitToInt() }
        return v
    }
}
