package com.francekim.empreintedelame.domain.calculator.humandesign

enum class HdLifeCycleId {
    SATURN_1, CHIRON_OPP, URANUS_OPP, CHIRON_RET, SATURN_2, SATURN_OPP, NODE_RETURN, URANUS_RET
}

data class HdLifeCycle(
    val id: HdLifeCycleId,
    val ageMin: Int,
    val ageMax: Int,
    val isActive: Boolean,
    val isPast: Boolean
)

object HdLifeCycleCalculator {

    fun calculateCycles(age: Int): List<HdLifeCycle> = listOf(
        cycle(HdLifeCycleId.SATURN_1,   28, 30, age),
        cycle(HdLifeCycleId.CHIRON_OPP, 38, 42, age),
        cycle(HdLifeCycleId.URANUS_OPP, 38, 42, age),
        cycle(HdLifeCycleId.CHIRON_RET, 50, 51, age),
        cycle(HdLifeCycleId.SATURN_2,   56, 60, age),
        cycle(HdLifeCycleId.SATURN_OPP, 72, 74, age),
        cycle(HdLifeCycleId.NODE_RETURN,74, 75, age),
        cycle(HdLifeCycleId.URANUS_RET, 84, 85, age)
    )

    // Liste (pas un seul élément) : deux cycles peuvent être actifs en même
    // temps, ex. Opposition de Saturne (72-74) et Retour des Nœuds (74-75)
    // se chevauchent exactement à 74 ans.
    fun activeCycles(age: Int): List<HdLifeCycle> =
        calculateCycles(age).filter { it.isActive }

    fun nextCycle(age: Int): HdLifeCycle? =
        calculateCycles(age).firstOrNull { !it.isActive && !it.isPast }

    private fun cycle(id: HdLifeCycleId, ageMin: Int, ageMax: Int, age: Int) = HdLifeCycle(
        id       = id,
        ageMin   = ageMin,
        ageMax   = ageMax,
        isActive = age in ageMin..ageMax,
        isPast   = age > ageMax
    )
}
