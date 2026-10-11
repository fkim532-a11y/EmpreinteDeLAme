package com.francekim.empreintedelame.domain.calculator.humandesign

import com.francekim.empreintedelame.data.model.UserProfile

enum class HDType { MANIFESTOR, GENERATOR, MANIFESTING_GENERATOR, PROJECTOR, REFLECTOR }
enum class HDAuthority { EMOTIONAL, SACRAL, SPLENIC, EGO, SELF_PROJECTED, MENTAL, LUNAR }
enum class HDDefinition { NONE, SINGLE, SPLIT, TRIPLE_SPLIT, QUADRUPLE_SPLIT }

data class Activation(val gate: Int, val line: Int)

data class HumanDesignChart(
    val type: HDType,
    val authority: HDAuthority,
    val profile: String, // ex. "3/5" — ligne Soleil Personality / ligne Soleil Design
    val definition: HDDefinition,
    val definedCenters: Set<Center>,
    val definedChannels: List<Channel>,
    val activeGates: Set<Int>
)

object HumanDesignCalculator {

    /** Retourne null si l'heure de naissance n'a pas été renseignée (0h00 = valeur par défaut, pas une vraie heure). */
    fun calculate(profile: UserProfile): HumanDesignChart? {
        if (profile.birthHour == 0 && profile.birthMinute == 0) return null

        val birthJd = Ephemeris.julianDay(
            profile.birthYear, profile.birthMonth, profile.birthDay,
            profile.birthHour, profile.birthMinute, profile.birthUtcOffsetMinutes
        )
        val designJd = Ephemeris.findDesignJd(birthJd)

        val personality = activationsAt(birthJd)
        val design = activationsAt(designJd)

        val activeGates = (personality.values.map { it.gate } + design.values.map { it.gate }).toSet()
        val definedChannels = HumanDesignTables.CHANNELS.filter { it.gateA in activeGates && it.gateB in activeGates }
        val definedCenters = definedChannels.flatMap { listOf(it.centerA, it.centerB) }.toSet()

        return HumanDesignChart(
            type            = determineType(definedCenters, definedChannels),
            authority       = determineAuthority(definedCenters),
            profile         = "${personality.getValue("SUN").line}/${design.getValue("SUN").line}",
            definition      = determineDefinition(definedCenters, definedChannels),
            definedCenters  = definedCenters,
            definedChannels = definedChannels,
            activeGates     = activeGates
        )
    }

    private fun norm360(deg: Double): Double {
        var d = deg % 360.0
        if (d < 0) d += 360.0
        return d
    }

    private fun activation(longitude: Double): Activation {
        val (gate, line) = HumanDesignTables.gateAndLine(longitude)
        return Activation(gate, line)
    }

    /** Les 13 activations (Soleil, Terre, Lune, Nœuds, 8 planètes) à un instant donné. */
    private fun activationsAt(jd: Double): Map<String, Activation> {
        val sunLon = Ephemeris.sunLongitude(jd)
        val nodeLon = Ephemeris.northNodeLongitude(jd)

        return linkedMapOf(
            "SUN" to activation(sunLon),
            "EARTH" to activation(norm360(sunLon + 180.0)),
            "MOON" to activation(Ephemeris.moonLongitude(jd)),
            "NORTH_NODE" to activation(nodeLon),
            "SOUTH_NODE" to activation(norm360(nodeLon + 180.0)),
            "MERCURY" to activation(Ephemeris.planetLongitude(Ephemeris.Planet.MERCURY, jd)),
            "VENUS" to activation(Ephemeris.planetLongitude(Ephemeris.Planet.VENUS, jd)),
            "MARS" to activation(Ephemeris.planetLongitude(Ephemeris.Planet.MARS, jd)),
            "JUPITER" to activation(Ephemeris.planetLongitude(Ephemeris.Planet.JUPITER, jd)),
            "SATURN" to activation(Ephemeris.planetLongitude(Ephemeris.Planet.SATURN, jd)),
            "URANUS" to activation(Ephemeris.planetLongitude(Ephemeris.Planet.URANUS, jd)),
            "NEPTUNE" to activation(Ephemeris.planetLongitude(Ephemeris.Planet.NEPTUNE, jd)),
            "PLUTO" to activation(Ephemeris.plutoLongitude(jd))
        )
    }

    private fun buildAdjacency(channels: List<Channel>): Map<Center, List<Center>> {
        val adjacency = mutableMapOf<Center, MutableList<Center>>()
        channels.forEach { channel ->
            adjacency.getOrPut(channel.centerA) { mutableListOf() }.add(channel.centerB)
            adjacency.getOrPut(channel.centerB) { mutableListOf() }.add(channel.centerA)
        }
        return adjacency
    }

    /** Vrai si `start` est relié à au moins un centre de `targets` via une chaîne de canaux définis. */
    private fun isConnectedTo(start: Center, targets: Set<Center>, channels: List<Channel>): Boolean {
        val adjacency = buildAdjacency(channels)
        val visited = mutableSetOf(start)
        val queue = ArrayDeque(listOf(start))
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            adjacency[current]?.forEach { next ->
                if (next !in visited) {
                    if (next in targets) return true
                    visited.add(next)
                    queue.add(next)
                }
            }
        }
        return false
    }

    private fun determineType(definedCenters: Set<Center>, definedChannels: List<Channel>): HDType {
        if (definedCenters.isEmpty()) return HDType.REFLECTOR

        val sacralDefined = Center.SACRAL in definedCenters
        val throatDefined = Center.THROAT in definedCenters
        val throatConnectedToMotor = throatDefined && isConnectedTo(Center.THROAT, Center.MOTORS, definedChannels)

        return when {
            sacralDefined && throatConnectedToMotor -> HDType.MANIFESTING_GENERATOR
            sacralDefined -> HDType.GENERATOR
            throatConnectedToMotor -> HDType.MANIFESTOR
            else -> HDType.PROJECTOR
        }
    }

    private fun determineAuthority(definedCenters: Set<Center>): HDAuthority = when {
        Center.SOLAR_PLEXUS in definedCenters -> HDAuthority.EMOTIONAL
        Center.SACRAL in definedCenters -> HDAuthority.SACRAL
        Center.SPLEEN in definedCenters -> HDAuthority.SPLENIC
        Center.HEART in definedCenters -> HDAuthority.EGO
        Center.G in definedCenters -> HDAuthority.SELF_PROJECTED
        Center.THROAT in definedCenters -> HDAuthority.MENTAL
        else -> HDAuthority.LUNAR
    }

    private fun determineDefinition(definedCenters: Set<Center>, definedChannels: List<Channel>): HDDefinition {
        if (definedCenters.isEmpty()) return HDDefinition.NONE
        val adjacency = buildAdjacency(definedChannels)
        val visited = mutableSetOf<Center>()
        var components = 0
        for (center in definedCenters) {
            if (center in visited) continue
            components++
            val queue = ArrayDeque(listOf(center))
            visited.add(center)
            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                adjacency[current]?.forEach { next ->
                    if (next !in visited && next in definedCenters) {
                        visited.add(next)
                        queue.add(next)
                    }
                }
            }
        }
        return when (components) {
            1 -> HDDefinition.SINGLE
            2 -> HDDefinition.SPLIT
            3 -> HDDefinition.TRIPLE_SPLIT
            else -> HDDefinition.QUADRUPLE_SPLIT
        }
    }
}
