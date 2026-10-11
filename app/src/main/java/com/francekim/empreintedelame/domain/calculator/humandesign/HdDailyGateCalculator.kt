package com.francekim.empreintedelame.domain.calculator.humandesign

data class HdDailyGate(
    val gate: Int,
    val line: Int,
    val hexagramLines: String  // 6 caractères '1'/'0', trait du bas en position 0
)

object HdDailyGateCalculator {

    // Motif des 6 traits (bas → haut) de chaque porte — numérotation King Wen
    // des 64 hexagrammes du Yi-King, '1' = trait plein (Yang), '0' = trait coupé (Yin).
    private val HEXAGRAM_LINES = mapOf(
        1  to "111111", 2  to "000000", 3  to "100010", 4  to "010001",
        5  to "111010", 6  to "010111", 7  to "010000", 8  to "000010",
        9  to "111011", 10 to "110111", 11 to "111000", 12 to "000111",
        13 to "101111", 14 to "111101", 15 to "001000", 16 to "000100",
        17 to "100110", 18 to "011001", 19 to "110000", 20 to "000011",
        21 to "100101", 22 to "101001", 23 to "000001", 24 to "100000",
        25 to "100111", 26 to "111001", 27 to "100001", 28 to "011110",
        29 to "010010", 30 to "101101", 31 to "001110", 32 to "011100",
        33 to "001111", 34 to "111100", 35 to "000101", 36 to "101000",
        37 to "101011", 38 to "110101", 39 to "001010", 40 to "010100",
        41 to "110001", 42 to "100011", 43 to "111110", 44 to "011111",
        45 to "000110", 46 to "011000", 47 to "010110", 48 to "011010",
        49 to "101110", 50 to "011101", 51 to "100100", 52 to "001001",
        53 to "001011", 54 to "110100", 55 to "101100", 56 to "001101",
        57 to "011011", 58 to "110110", 59 to "010011", 60 to "110010",
        61 to "110011", 62 to "001100", 63 to "101010", 64 to "010101"
    )

    // Porte personnelle du jour : mélange jour/mois de naissance (propre au profil)
    // et jour/mois/année actuels (fait varier le résultat chaque jour).
    fun personalGate(
        birthDay    : Int,
        birthMonth  : Int,
        currentDay  : Int,
        currentMonth: Int,
        currentYear : Int
    ): HdDailyGate {
        val sum = birthDay + birthMonth + currentDay + currentMonth + currentYear
        val gateNumber = (sum % 64) + 1
        val line = (sum % 6) + 1
        return HdDailyGate(gate = gateNumber, line = line, hexagramLines = HEXAGRAM_LINES.getValue(gateNumber))
    }
}
