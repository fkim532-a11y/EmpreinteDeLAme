package com.francekim.empreintedelame.domain.calculator

// ─── Réduction numérique ───────────────────────────────────────────────────────
// Réduit n à 1-9 en préservant les nombres maîtres 11, 22, 33.
fun reduce(n: Int): Int {
    var value = n
    while (value > 9 && value != 11 && value != 22 && value != 33) {
        value = value.toString().sumOf { it.digitToInt() }
    }
    return value
}

// Variante prédictive : préserve uniquement 11 et 22 (utilisé pour cycles et années).
fun reducePredictive(n: Int): Int {
    var value = n
    while (value > 9 && value != 11 && value != 22) {
        value = value.toString().sumOf { it.digitToInt() }
    }
    return value
}

// ─── Tableau de Pythagore ──────────────────────────────────────────────────────
// Normalise les accents et ligatures avant conversion.
fun normalizeAccents(input: String): String =
    input.uppercase()
        .replace("Œ", "OE")   // ligatures en premier
        .replace("Æ", "AE")
        .replace("É", "E").replace("È", "E").replace("Ê", "E").replace("Ë", "E")
        .replace("À", "A").replace("Â", "A").replace("Ä", "A")
        .replace("Î", "I").replace("Ï", "I")
        .replace("Ô", "O").replace("Ö", "O")
        .replace("Ù", "U").replace("Û", "U").replace("Ü", "U")
        .replace("Ç", "C")

// Convertit une lettre normalisée (A-Z) en valeur Pythagorée 1-9.
fun letterToDigit(c: Char): Int? = when (c) {
    'A', 'J', 'S' -> 1
    'B', 'K', 'T' -> 2
    'C', 'L', 'U' -> 3
    'D', 'M', 'V' -> 4
    'E', 'N', 'W' -> 5
    'F', 'O', 'X' -> 6
    'G', 'P', 'Y' -> 7
    'H', 'Q', 'Z' -> 8
    'I', 'R'      -> 9
    else          -> null
}
