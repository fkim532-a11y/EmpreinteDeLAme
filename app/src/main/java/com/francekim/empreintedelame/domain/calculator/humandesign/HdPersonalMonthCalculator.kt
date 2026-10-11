package com.francekim.empreintedelame.domain.calculator.humandesign

import com.francekim.empreintedelame.domain.calculator.reducePredictive

// Même formule que le Mois Personnel du module Numérologie
// (voir PredictiveCalculator.calculate).
object HdPersonalMonthCalculator {

    fun calculate(personalYear: Int, currentMonth: Int): Int =
        reducePredictive(personalYear + reducePredictive(currentMonth))
}
