package com.francekim.empreintedelame.domain.calculator

import com.francekim.empreintedelame.data.model.UserProfile
import kotlin.math.abs

data class DestinyProfile(
    // Chemin de Vie
    val lifePathSubTotal: Int,      // ex: 16
    val lifePathReduced: Int,       // ex: 7
    val lifePathDisplay: String,    // "16/7" ou "7" ou "11"

    // Nombre de l'Ombre
    val shadowNumber: Int,

    // Nombre de Maturité (null tant que le bloc Identité n'est pas calculé)
    val maturityNumber: Int?,

    // 4 Sommets
    val summit1: Int,
    val summit2: Int,
    val summit3: Int,
    val summit4: Int,

    // Âge de chaque sommet (S4 commence après ageSummit3)
    val ageSummit1: Int,
    val ageSummit2: Int,
    val ageSummit3: Int,

    // Année Personnelle
    val universalYear: Int,
    val personalYear: Int,
    val yearChallenge: Int
)

object DestinyCalculator {

    fun calculate(
        profile: UserProfile,
        currentYear: Int,
        expressionNumber: Int? = null
    ): DestinyProfile {
        val rDay   = reduce(profile.birthDay)
        val rMonth = reduce(profile.birthMonth)
        val rYear  = reduce(profile.birthYear)

        // Chemin de Vie
        val subTotal        = rDay + rMonth + rYear
        val lifePathReduced = reduce(subTotal)
        val lifePathDisplay = if (subTotal != lifePathReduced) "$subTotal/$lifePathReduced"
                              else "$lifePathReduced"

        // Nombre de l'Ombre
        val shadowNumber = abs(rDay - rMonth)

        // Nombre de Maturité (Bloc 2 requis)
        val maturityNumber = expressionNumber?.let { reduce(lifePathReduced + it) }

        // 4 Sommets
        val s1 = reduce(rDay + rMonth)
        val s2 = reduce(rDay + rYear)
        val s3 = reduce(s1 + s2)
        val s4 = reduce(rMonth + rYear)

        // Âges des sommets
        val lifePathForAge = when (lifePathReduced) {
            11   -> 2
            22   -> 4
            else -> lifePathReduced
        }
        val ageS1 = 36 - lifePathForAge
        val ageS2 = ageS1 + 9
        val ageS3 = ageS2 + 9

        // Année Personnelle
        val universalYear = reduce(currentYear)
        val personalYear  = reduce(rDay + rMonth + universalYear)
        val yearChallenge = reduce(abs(personalYear - universalYear))

        return DestinyProfile(
            lifePathSubTotal = subTotal,
            lifePathReduced  = lifePathReduced,
            lifePathDisplay  = lifePathDisplay,
            shadowNumber     = shadowNumber,
            maturityNumber   = maturityNumber,
            summit1          = s1,
            summit2          = s2,
            summit3          = s3,
            summit4          = s4,
            ageSummit1       = ageS1,
            ageSummit2       = ageS2,
            ageSummit3       = ageS3,
            universalYear    = universalYear,
            personalYear     = personalYear,
            yearChallenge    = yearChallenge
        )
    }
}
