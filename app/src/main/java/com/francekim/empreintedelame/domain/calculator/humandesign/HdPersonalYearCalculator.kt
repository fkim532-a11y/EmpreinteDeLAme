package com.francekim.empreintedelame.domain.calculator.humandesign

import com.francekim.empreintedelame.domain.calculator.reducePredictive

// Même formule que l'Année Personnelle du module Numérologie
// (voir PredictiveCalculator.calculate) — il s'agit du même nombre,
// simplement lu avec un angle Human Design plutôt que numérologique.
object HdPersonalYearCalculator {

    fun calculate(birthDay: Int, birthMonth: Int, currentYear: Int): Int {
        val dayR   = reducePredictive(birthDay)
        val monthR = reducePredictive(birthMonth)
        val yearR  = reducePredictive(currentYear.toString().sumOf { it.digitToInt() })
        return reducePredictive(dayR + monthR + yearR)
    }
}
