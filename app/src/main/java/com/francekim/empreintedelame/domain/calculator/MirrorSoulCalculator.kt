package com.francekim.empreintedelame.domain.calculator

object MirrorSoulCalculator {

    /** Chemin de vie réduit (nombres maîtres 11/22/33 préservés), sous forme de clé JSON ("1".."9", "11", "22", "33"). */
    fun lifePathKey(birthDay: Int, birthMonth: Int, birthYear: Int): String {
        val lifePath = reduce(reduce(birthDay) + reduce(birthMonth) + reduce(birthYear))
        return lifePath.toString()
    }
}
