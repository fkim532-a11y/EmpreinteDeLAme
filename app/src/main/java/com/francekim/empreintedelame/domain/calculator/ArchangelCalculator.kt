package com.francekim.empreintedelame.domain.calculator

import com.francekim.empreintedelame.R
import com.francekim.empreintedelame.data.model.UserProfile

enum class Archangel(val key: String, val lifePath: Int, val imageRes: Int) {
    MICHAEL(   "michael",    1,  R.drawable.archangel_michael),
    CHAMUEL(   "chamuel",    2,  R.drawable.archangel_chamuel),
    GABRIEL(   "gabriel",    3,  R.drawable.archangel_gabriel),
    URIEL(     "uriel",      4,  R.drawable.archangel_uriel),
    ARIEL(     "ariel",      5,  R.drawable.archangel_ariel),
    RAPHAEL(   "raphael",    6,  R.drawable.archangel_raphael),
    JOPHIEL(   "jophiel",    7,  R.drawable.archangel_jophiel),
    RAGUEL(    "raguel",     8,  R.drawable.archangel_raguel),
    JEREMIEL(  "jeremiel",   9,  R.drawable.archangel_jeremiel),
    HANIEL(    "haniel",    11,  R.drawable.archangel_haniel),
    SANDALPHON("sandalphon",22,  R.drawable.archangel_sandalphon),
    METATRON(  "metatron",  33,  R.drawable.archangel_metatron);

    companion object {
        fun fromLifePath(n: Int): Archangel = entries.first { it.lifePath == n }
    }
}

object ArchangelCalculator {

    fun calculate(profile: UserProfile): Archangel {
        val rDay   = reduce(profile.birthDay)
        val rMonth = reduce(profile.birthMonth)
        val rYear  = reduce(profile.birthYear)
        val lifePathReduced = reduce(rDay + rMonth + rYear)
        return Archangel.fromLifePath(lifePathReduced)
    }
}
