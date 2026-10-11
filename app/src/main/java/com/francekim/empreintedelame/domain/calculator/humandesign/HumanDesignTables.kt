package com.francekim.empreintedelame.domain.calculator.humandesign

// ─── Tables de référence Human Design ──────────────────────────────────────────
// Constantes pures du système Human Design (roue des 64 portes, 9 centres, 36 canaux).
// Ces tables sont figées et publiques — aucun calcul, juste la structure du BodyGraph.

enum class Center {
    HEAD, AJNA, THROAT, G, HEART, SACRAL, SPLEEN, SOLAR_PLEXUS, ROOT;

    companion object {
        /** Les 4 centres "moteurs" — leur définition alimente le Type (Manifestor/Generator...). */
        val MOTORS = setOf(HEART, SACRAL, SOLAR_PLEXUS, ROOT)
    }
}

data class Channel(val gateA: Int, val centerA: Center, val gateB: Int, val centerB: Center) {
    fun otherCenter(center: Center): Center? = when (center) {
        centerA -> centerB
        centerB -> centerA
        else -> null
    }
    fun touches(center: Center) = center == centerA || center == centerB
}

object HumanDesignTables {

    /**
     * Ordre des 64 portes autour de la roue zodiacale, en partant de la Porte 41
     * qui débute à 302° de longitude écliptique tropicale (repère vérifié).
     * Chaque porte occupe exactement 360/64 = 5.625° ; chaque ligne, 5.625/6 = 0.9375°.
     */
    const val WHEEL_START_DEGREES = 302.0
    const val GATE_WIDTH = 5.625
    const val LINE_WIDTH = GATE_WIDTH / 6.0

    val GATE_WHEEL: List<Int> = listOf(
        41, 19, 13, 49, 30, 55, 37, 63, 22, 36,
        25, 17, 21, 51, 42, 3, 27, 24, 2, 23,
        8, 20, 16, 35, 45, 12, 15, 52, 39, 53,
        62, 56, 31, 33, 7, 4, 29, 59, 40, 64,
        47, 6, 46, 18, 48, 57, 32, 50, 28, 44,
        1, 43, 14, 34, 9, 5, 26, 11, 10, 58,
        38, 54, 61, 60
    )

    /** Retourne (porte, ligne 1-6) pour une longitude écliptique géocentrique donnée. */
    fun gateAndLine(longitude: Double): Pair<Int, Int> {
        var offset = (longitude - WHEEL_START_DEGREES) % 360.0
        if (offset < 0) offset += 360.0
        val gateIndex = (offset / GATE_WIDTH).toInt().coerceIn(0, 63)
        val withinGate = offset - gateIndex * GATE_WIDTH
        val line = (withinGate / LINE_WIDTH).toInt().coerceIn(0, 5) + 1
        return GATE_WHEEL[gateIndex] to line
    }

    /** Les 36 canaux : chaque canal relie 2 portes appartenant à 2 centres. */
    val CHANNELS: List<Channel> = listOf(
        Channel(1, Center.G, 8, Center.THROAT),
        Channel(2, Center.G, 14, Center.SACRAL),
        Channel(3, Center.SACRAL, 60, Center.ROOT),
        Channel(4, Center.AJNA, 63, Center.HEAD),
        Channel(5, Center.SACRAL, 15, Center.G),
        Channel(6, Center.SOLAR_PLEXUS, 59, Center.SACRAL),
        Channel(7, Center.G, 31, Center.THROAT),
        Channel(9, Center.SACRAL, 52, Center.ROOT),
        Channel(10, Center.G, 20, Center.THROAT),
        Channel(10, Center.G, 34, Center.SACRAL),
        Channel(10, Center.G, 57, Center.SPLEEN),
        Channel(11, Center.AJNA, 56, Center.THROAT),
        Channel(12, Center.THROAT, 22, Center.SOLAR_PLEXUS),
        Channel(13, Center.G, 33, Center.THROAT),
        Channel(16, Center.THROAT, 48, Center.SPLEEN),
        Channel(17, Center.AJNA, 62, Center.THROAT),
        Channel(18, Center.SPLEEN, 58, Center.ROOT),
        Channel(19, Center.ROOT, 49, Center.SOLAR_PLEXUS),
        Channel(20, Center.THROAT, 34, Center.SACRAL),
        Channel(20, Center.THROAT, 57, Center.SPLEEN),
        Channel(21, Center.HEART, 45, Center.THROAT),
        Channel(23, Center.THROAT, 43, Center.AJNA),
        Channel(24, Center.AJNA, 61, Center.HEAD),
        Channel(25, Center.G, 51, Center.HEART),
        Channel(26, Center.HEART, 44, Center.SPLEEN),
        Channel(27, Center.SACRAL, 50, Center.SPLEEN),
        Channel(28, Center.SPLEEN, 38, Center.ROOT),
        Channel(29, Center.SACRAL, 46, Center.G),
        Channel(30, Center.SOLAR_PLEXUS, 41, Center.ROOT),
        Channel(32, Center.SPLEEN, 54, Center.ROOT),
        Channel(34, Center.SACRAL, 57, Center.SPLEEN),
        Channel(35, Center.THROAT, 36, Center.SOLAR_PLEXUS),
        Channel(37, Center.SOLAR_PLEXUS, 40, Center.HEART),
        Channel(39, Center.ROOT, 55, Center.SOLAR_PLEXUS),
        Channel(42, Center.SACRAL, 53, Center.ROOT),
        Channel(47, Center.AJNA, 64, Center.HEAD)
    )

    /** Centre auquel appartient chaque porte (dérivé des canaux, une porte = un centre fixe). */
    val GATE_CENTER: Map<Int, Center> = buildMap {
        CHANNELS.forEach { channel ->
            put(channel.gateA, channel.centerA)
            put(channel.gateB, channel.centerB)
        }
    }
}
