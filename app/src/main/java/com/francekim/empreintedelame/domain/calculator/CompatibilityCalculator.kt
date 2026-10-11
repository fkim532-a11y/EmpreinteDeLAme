package com.francekim.empreintedelame.domain.calculator

import com.francekim.empreintedelame.data.model.UserProfile
import com.francekim.empreintedelame.data.repository.CompatibilityRepository
import com.francekim.empreintedelame.domain.calculator.humandesign.Center
import com.francekim.empreintedelame.domain.calculator.humandesign.HumanDesignCalculator
import com.francekim.empreintedelame.domain.model.CompatibilityEntry
import java.util.Calendar

data class CompatibilityCenterResult(
    val center   : Center,
    val definedA : Boolean,
    val definedB : Boolean,
    val text     : String?
)

data class CompatibilityResult(
    val globalScore     : Int,
    val lifePathEntry   : CompatibilityEntry?,
    val hdTypeEntry     : CompatibilityEntry?,
    val centerResults   : List<CompatibilityCenterResult>,
    val hasHumanDesign  : Boolean
)

object CompatibilityCalculator {

    fun compute(profileA: UserProfile, profileB: UserProfile, repo: CompatibilityRepository): CompatibilityResult {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val destinyA = DestinyCalculator.calculate(profileA, currentYear)
        val destinyB = DestinyCalculator.calculate(profileB, currentYear)
        val lifePathEntry = repo.lifePathCompatibility(destinyA.lifePathReduced, destinyB.lifePathReduced)

        val chartA = HumanDesignCalculator.calculate(profileA)
        val chartB = HumanDesignCalculator.calculate(profileB)
        val hasHumanDesign = chartA != null && chartB != null

        val hdTypeEntry = if (hasHumanDesign) {
            repo.hdTypeCompatibility(chartA!!.type, chartB!!.type)
        } else null

        val centerResults = if (hasHumanDesign) {
            Center.entries.map { center ->
                val definedA = center in chartA!!.definedCenters
                val definedB = center in chartB!!.definedCenters
                CompatibilityCenterResult(
                    center   = center,
                    definedA = definedA,
                    definedB = definedB,
                    text     = repo.centerCompatibility(center, definedA, definedB)
                )
            }
        } else emptyList()

        val scores = listOfNotNull(lifePathEntry?.score, hdTypeEntry?.score)
        val globalScore = if (scores.isNotEmpty()) scores.sum() / scores.size else 0

        return CompatibilityResult(
            globalScore    = globalScore,
            lifePathEntry  = lifePathEntry,
            hdTypeEntry    = hdTypeEntry,
            centerResults  = centerResults,
            hasHumanDesign = hasHumanDesign
        )
    }
}
