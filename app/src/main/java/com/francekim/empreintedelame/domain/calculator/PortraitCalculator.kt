package com.francekim.empreintedelame.domain.calculator

// ─── Structures de données ─────────────────────────────────────────────────────

/**
 * Résultat de la Grille de l'Âme (Nombre de l'Inclusion).
 * [occurrences] : pour chaque chiffre 1-9, combien de fois il apparaît dans le nom.
 * [missingNumbers]    : chiffres absents (cases karmiques).
 * [overloadedNumbers] : chiffres en survoltage (> 4 occurrences).
 */
data class InclusionGrid(
    val occurrences      : Map<Int, Int>,
    val missingNumbers   : List<Int>,
    val overloadedNumbers: List<Int>
)

/**
 * Les 8 flèches possibles dans la Table de Pythagore.
 *   Lignes  : PRATICIEN(1-4-7), VOLONTE(2-5-8), INTELLECT(3-6-9)
 *   Colonnes: PENSEE(1-2-3), PERSEVERANCE(4-5-6), SPIRITUALITE(7-8-9)
 *   Diag.   : DIAGONAL_159, DIAGONAL_357
 */
enum class Arrow {
    PRATICIEN,      // 1-4-7 — ligne basse
    VOLONTE,        // 2-5-8 — ligne médiane
    INTELLECT,      // 3-6-9 — ligne haute
    PENSEE,         // 1-2-3 — colonne gauche
    PERSEVERANCE,   // 4-5-6 — colonne centrale
    SPIRITUALITE,   // 7-8-9 — colonne droite
    DIAGONAL_159,   // 1-5-9 — diagonale principale
    DIAGONAL_357    // 3-5-7 — diagonale secondaire
}

/**
 * Flèches pleines = tous les chiffres présents dans la date.
 * Flèches vides   = tous les chiffres ABSENTS de la date (défis d'apprentissage).
 */
data class PythagoreanArrows(
    val fullArrows : List<Arrow>,
    val emptyArrows: List<Arrow>
)

/**
 * Résultat complet du module Portrait (Bloc 2).
 */
data class PortraitProfile(
    val inclusionGrid : InclusionGrid,
    val balanceNumber : Int,           // Nombre de l'Inconscient — peut être 11, 22 ou 33
    val arrows        : PythagoreanArrows,
    val lifePathNumber: Int            // Chemin de vie réduit en chiffre simple 1-9 (nombres maîtres inclus)
)

// ─── Calculateur ───────────────────────────────────────────────────────────────

object PortraitCalculator {

    private val ARROW_DIGITS = mapOf(
        Arrow.PRATICIEN     to setOf(1, 4, 7),
        Arrow.VOLONTE       to setOf(2, 5, 8),
        Arrow.INTELLECT     to setOf(3, 6, 9),
        Arrow.PENSEE        to setOf(1, 2, 3),
        Arrow.PERSEVERANCE  to setOf(4, 5, 6),
        Arrow.SPIRITUALITE  to setOf(7, 8, 9),
        Arrow.DIAGONAL_159  to setOf(1, 5, 9),
        Arrow.DIAGONAL_357  to setOf(3, 5, 7)
    )

    fun calculate(
        firstName: String,
        lastName : String,
        birthDay : Int,
        birthMonth: Int,
        birthYear : Int
    ): PortraitProfile {
        val fullName = "$firstName $lastName"
        return PortraitProfile(
            inclusionGrid  = calculateInclusionGrid(fullName),
            balanceNumber  = calculateBalanceNumber(fullName),
            arrows         = calculateArrows(birthDay, birthMonth, birthYear),
            lifePathNumber = calculateLifePathNumber(birthDay, birthMonth, birthYear)
        )
    }

    // ── 4. Chemin de Vie (réduit en chiffre simple) ────────────────────────────
    // Sert uniquement à comparer avec un chiffre manquant de la Grille de l'Âme (1-9).
    // Contrairement au Chemin de Vie affiché dans le module Destinée, les nombres
    // maîtres (11, 22, 33) sont ici réduits complètement (11→2, 22→4, 33→6).
    fun calculateLifePathNumber(birthDay: Int, birthMonth: Int, birthYear: Int): Int {
        val rDay   = reduce(birthDay)
        val rMonth = reduce(birthMonth)
        val rYear  = reduce(birthYear)
        var total  = rDay + rMonth + rYear
        while (total > 9) total = total.toString().sumOf { it.digitToInt() }
        return total
    }

    // ── 1. Grille de l'Âme ─────────────────────────────────────────────────────
    // Compte les occurrences de chaque valeur Pythagorée (1-9) dans le nom complet.
    fun calculateInclusionGrid(fullName: String): InclusionGrid {
        val normalized = normalizeAccents(fullName)
        val occurrences: Map<Int, Int> = (1..9).associateWith { digit ->
            normalized.count { c -> letterToDigit(c) == digit }
        }
        return InclusionGrid(
            occurrences       = occurrences,
            missingNumbers    = occurrences.filter { it.value == 0 }.keys.sorted(),
            overloadedNumbers = occurrences.filter { it.value >  4 }.keys.sorted()
        )
    }

    // ── 2. Nombre de l'Inconscient ─────────────────────────────────────────────
    // Compte le nombre total de lettres (sans conversion), réduit en préservant 11/22/33.
    fun calculateBalanceNumber(fullName: String): Int {
        val letterCount = fullName.count { it.isLetter() }
        return reduce(letterCount)
    }

    // ── 3. Flèches de Pythagore ────────────────────────────────────────────────
    // Chaque chiffre de la date (format JJ/MM/AAAA) est placé dans la matrice :
    //   [3, 6, 9]
    //   [2, 5, 8]
    //   [1, 4, 7]
    // Flèche pleine : tous les chiffres du groupe présents dans la date.
    // Flèche vide   : aucun chiffre du groupe présent dans la date.
    fun calculateArrows(birthDay: Int, birthMonth: Int, birthYear: Int): PythagoreanArrows {
        val raw = "%02d%02d%04d".format(birthDay, birthMonth, birthYear)
        val digitsInDate: Set<Int> = raw.mapNotNull { c ->
            c.digitToInt().takeIf { it in 1..9 }  // 0 ignoré (hors matrice)
        }.toSet()

        val fullArrows  = mutableListOf<Arrow>()
        val emptyArrows = mutableListOf<Arrow>()

        ARROW_DIGITS.forEach { (arrow, digits) ->
            when {
                digits.all  { it in digitsInDate } -> fullArrows.add(arrow)
                digits.none { it in digitsInDate } -> emptyArrows.add(arrow)
            }
        }
        return PythagoreanArrows(fullArrows.toList(), emptyArrows.toList())
    }
}
