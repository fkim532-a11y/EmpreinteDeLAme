package com.francekim.empreintedelame.domain.calculator

import kotlin.random.Random

// Une ligne = 3 pièces (true = face caractères = Yang = 3 points, false = face dragon-phénix = Yin = 2 points).
// Somme : 6 = Yin mutant (vieux Yin), 7 = Yang stable (jeune Yang), 8 = Yin stable (jeune Yin), 9 = Yang mutant (vieux Yang).
data class YiKingLine(val coins: List<Boolean>) {
    val value: Int get() = coins.sumOf { if (it) 3 else 2 }
    val isYang: Boolean get() = value == 7 || value == 9
    val isMutant: Boolean get() = value == 6 || value == 9

    companion object {
        // Reconstruit une ligne à partir d'une valeur stockée (historique) — la répartition
        // exacte des 3 pièces n'a pas besoin d'être fidèle à l'original, seule la somme compte.
        fun fromValue(value: Int): YiKingLine = when (value) {
            6    -> YiKingLine(listOf(false, false, false))
            7    -> YiKingLine(listOf(true, false, false))
            8    -> YiKingLine(listOf(false, true, true))
            9    -> YiKingLine(listOf(true, true, true))
            else -> throw IllegalArgumentException("Valeur de trait invalide : $value")
        }
    }
}

data class YiKingCastResult(
    val lines: List<YiKingLine>,
    val primaryLinePattern: String,
    val primaryNumero: Int,
    val secondaryNumero: Int?
)

object YiKingCastCalculator {

    fun tossLine(random: Random = Random.Default): YiKingLine =
        YiKingLine(List(3) { random.nextBoolean() })

    fun buildCast(lines: List<YiKingLine>): YiKingCastResult {
        require(lines.size == 6) { "Un tirage Yi-King nécessite exactement 6 traits" }

        val primaryPattern = lines.joinToString("") { if (it.isYang) "1" else "0" }
        val primaryNumero = YiKingTables.NUMERO_FOR_LINES.getValue(primaryPattern)

        val secondaryNumero = if (lines.any { it.isMutant }) {
            val secondaryPattern = lines.joinToString("") { line ->
                when {
                    !line.isMutant -> if (line.isYang) "1" else "0"
                    else           -> if (line.isYang) "0" else "1" // le trait mutant s'inverse
                }
            }
            YiKingTables.NUMERO_FOR_LINES[secondaryPattern]
        } else null

        return YiKingCastResult(lines, primaryPattern, primaryNumero, secondaryNumero)
    }
}
