package com.francekim.empreintedelame.domain.calculator

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.PI
import kotlin.math.sin

enum class BiorhythmCycle(val periodDays: Int) {
    PHYSICAL(23),
    EMOTIONAL(28),
    INTELLECTUAL(33)
}

enum class BiorhythmState { HIGH, LOW, CRITICAL }

data class BiorhythmCycleResult(
    val cycle: BiorhythmCycle,
    val value: Double,
    val state: BiorhythmState
)

data class BiorhythmResult(
    val daysElapsed  : Long,
    val physical     : BiorhythmCycleResult,
    val emotional    : BiorhythmCycleResult,
    val intellectual : BiorhythmCycleResult
) {
    val synthesisKey: String
        get() = "PHYSICAL-${physical.state}_EMOTIONAL-${emotional.state}_INTELLECTUAL-${intellectual.state}"
}

object BiorhythmCalculator {

    fun valueAt(daysElapsed: Long, cycle: BiorhythmCycle): Double =
        sin(2 * PI * daysElapsed / cycle.periodDays)

    /** Un jour est critique quand la courbe traverse le zéro (environ 2 jours par cycle). */
    private const val CRITICAL_THRESHOLD = 0.15

    fun stateOf(value: Double): BiorhythmState = when {
        value > CRITICAL_THRESHOLD  -> BiorhythmState.HIGH
        value < -CRITICAL_THRESHOLD -> BiorhythmState.LOW
        else                        -> BiorhythmState.CRITICAL
    }

    fun calculate(
        birthDay      : Int,
        birthMonth    : Int,
        birthYear     : Int,
        referenceDate : LocalDate = LocalDate.now()
    ): BiorhythmResult {
        val birthDate   = LocalDate.of(birthYear, birthMonth, birthDay)
        val daysElapsed = ChronoUnit.DAYS.between(birthDate, referenceDate)

        fun resultFor(cycle: BiorhythmCycle): BiorhythmCycleResult {
            val value = valueAt(daysElapsed, cycle)
            return BiorhythmCycleResult(cycle, value, stateOf(value))
        }

        return BiorhythmResult(
            daysElapsed  = daysElapsed,
            physical     = resultFor(BiorhythmCycle.PHYSICAL),
            emotional    = resultFor(BiorhythmCycle.EMOTIONAL),
            intellectual = resultFor(BiorhythmCycle.INTELLECTUAL)
        )
    }
}
