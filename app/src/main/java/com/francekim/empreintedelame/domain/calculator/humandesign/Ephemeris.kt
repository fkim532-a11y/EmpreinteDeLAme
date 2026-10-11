package com.francekim.empreintedelame.domain.calculator.humandesign

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// ─── Éphéméride basse précision ────────────────────────────────────────────────
// Implémentation des formules classiques de Paul Schlyter ("How to Compute
// Planetary Positions", stjarnhimlen.se/comp/ppcomp.html) — méthode publique et
// éprouvée, précision ~1' d'arc pour les planètes, ~0.3° pour la Lune (largement
// suffisant : une ligne Human Design fait 56' d'arc). Aucune dépendance externe :
// tout est calculé analytiquement en Kotlin pur.
//
// Toutes les longitudes retournées sont écliptiques géocentriques, en degrés [0,360).
object Ephemeris {

    enum class Planet { MERCURY, VENUS, MARS, JUPITER, SATURN, URANUS, NEPTUNE }

    private const val DEG = Math.PI / 180.0

    private fun sinD(deg: Double) = sin(deg * DEG)
    private fun cosD(deg: Double) = cos(deg * DEG)
    private fun atan2D(y: Double, x: Double) = atan2(y, x) / DEG

    private fun norm360(deg: Double): Double {
        var d = deg % 360.0
        if (d < 0) d += 360.0
        return d
    }

    /**
     * Jour Julien (UTC) à partir d'une date/heure locale + décalage UTC en minutes.
     */
    fun julianDay(year: Int, month: Int, day: Int, hour: Int, minute: Int, utcOffsetMinutes: Int): Double {
        val a = (14 - month) / 12
        val y = year + 4800 - a
        val m = month + 12 * a - 3
        val jdn = day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
        val dayFraction = (hour + minute / 60.0 - utcOffsetMinutes / 60.0) / 24.0
        return jdn - 0.5 + dayFraction
    }

    /** Nombre de jours depuis l'époque de Schlyter (1999-12-31 0h UT = JD 2451543.5). */
    private fun dayNumber(jd: Double) = jd - 2451543.5

    /** Résout l'équation de Kepler E - e·sin(E) = M (E, M en degrés) par itération de Newton. */
    private fun solveKepler(mDeg: Double, e: Double): Double {
        var e0 = mDeg + e * (180.0 / Math.PI) * sinD(mDeg) * (1.0 + e * cosD(mDeg))
        repeat(8) {
            val diff = e0 - e * (180.0 / Math.PI) * sinD(e0) - mDeg
            val delta = diff / (1.0 - e * cosD(e0))
            e0 -= delta
            if (kotlin.math.abs(delta) < 1e-6) return e0
        }
        return e0
    }

    private data class Elements(val n: Double, val i: Double, val w: Double, val a: Double, val e: Double, val m: Double)

    /** Position héliocentrique (xh, yh) déduite des éléments orbitaux à un instant donné. */
    private fun heliocentricXY(el: Elements): Pair<Double, Double> {
        val E = solveKepler(el.m, el.e)
        val xv = el.a * (cosD(E) - el.e)
        val yv = el.a * sqrt(1.0 - el.e * el.e) * sinD(E)
        val r = sqrt(xv * xv + yv * yv)
        val v = atan2D(yv, xv)
        val xh = r * (cosD(el.n) * cosD(v + el.w) - sinD(el.n) * sinD(v + el.w) * cosD(el.i))
        val yh = r * (sinD(el.n) * cosD(v + el.w) + cosD(el.n) * sinD(v + el.w) * cosD(el.i))
        return xh to yh
    }

    private fun sunElements(d: Double) = Elements(
        n = 0.0, i = 0.0,
        w = 282.9404 + 4.70935e-5 * d,
        a = 1.0,
        e = 0.016709 - 1.151e-9 * d,
        m = norm360(356.0470 + 0.9856002585 * d)
    )

    /** Position géocentrique du Soleil (xs, ys) — sert aussi à géocentrer les planètes. */
    private fun sunGeocentricXY(d: Double): Pair<Double, Double> {
        val el = sunElements(d)
        val E = solveKepler(el.m, el.e)
        val xv = cosD(E) - el.e
        val yv = sqrt(1.0 - el.e * el.e) * sinD(E)
        val r = sqrt(xv * xv + yv * yv)
        val v = atan2D(yv, xv)
        val lonSun = norm360(v + el.w)
        return (r * cosD(lonSun)) to (r * sinD(lonSun))
    }

    fun sunLongitude(jd: Double): Double {
        val d = dayNumber(jd)
        val el = sunElements(d)
        val E = solveKepler(el.m, el.e)
        val xv = cosD(E) - el.e
        val yv = sqrt(1.0 - el.e * el.e) * sinD(E)
        val v = atan2D(yv, xv)
        return norm360(v + el.w)
    }

    private fun moonElements(d: Double) = Elements(
        n = norm360(125.1228 - 0.0529538083 * d),
        i = 5.1454,
        w = norm360(318.0634 + 0.1643573223 * d),
        a = 60.2666,
        e = 0.054900,
        m = norm360(115.3654 + 13.0649929509 * d)
    )

    fun northNodeLongitude(jd: Double): Double = moonElements(dayNumber(jd)).n

    fun moonLongitude(jd: Double): Double {
        val d = dayNumber(jd)
        val moon = moonElements(d)
        val (xh, yh) = heliocentricXY(moon) // "héliocentrique" ici = géocentrique : la Lune orbite la Terre
        var lon = norm360(atan2D(yh, xh))

        val sun = sunElements(d)
        val ms = sun.m
        val ls = norm360(ms + sun.w)
        val lm = norm360(moon.m + moon.w + moon.n)
        val mm = moon.m
        val f = norm360(lm - moon.n)
        val dElong = norm360(lm - ls)

        lon += -1.274 * sinD(mm - 2 * dElong)
        lon += 0.658 * sinD(2 * dElong)
        lon += -0.186 * sinD(ms)
        lon += -0.059 * sinD(2 * mm - 2 * dElong)
        lon += -0.057 * sinD(mm - 2 * dElong + ms)
        lon += 0.053 * sinD(mm + 2 * dElong)
        lon += 0.046 * sinD(2 * dElong - ms)
        lon += 0.041 * sinD(mm - ms)
        lon += -0.035 * sinD(dElong)
        lon += -0.031 * sinD(mm + ms)
        lon += -0.015 * sinD(2 * f - 2 * dElong)
        lon += 0.011 * sinD(mm - 4 * dElong)

        return norm360(lon)
    }

    private fun planetElements(planet: Planet, d: Double): Elements = when (planet) {
        Planet.MERCURY -> Elements(
            n = norm360(48.3313 + 3.24587e-5 * d), i = 7.0047 + 5.00e-8 * d,
            w = norm360(29.1241 + 1.01444e-5 * d), a = 0.387098,
            e = 0.205635 + 5.59e-10 * d, m = norm360(168.6562 + 4.0923344368 * d)
        )
        Planet.VENUS -> Elements(
            n = norm360(76.6799 + 2.46590e-5 * d), i = 3.3946 + 2.75e-8 * d,
            w = norm360(54.8910 + 1.38374e-5 * d), a = 0.723330,
            e = 0.006773 - 1.302e-9 * d, m = norm360(48.0052 + 1.6021302244 * d)
        )
        Planet.MARS -> Elements(
            n = norm360(49.5574 + 2.11081e-5 * d), i = 1.8497 - 1.78e-8 * d,
            w = norm360(286.5016 + 2.92961e-5 * d), a = 1.523688,
            e = 0.093405 + 2.516e-9 * d, m = norm360(18.6021 + 0.5240207766 * d)
        )
        Planet.JUPITER -> Elements(
            n = norm360(100.4542 + 2.76854e-5 * d), i = 1.3030 - 1.557e-7 * d,
            w = norm360(273.8777 + 1.64505e-5 * d), a = 5.20256,
            e = 0.048498 + 4.469e-9 * d, m = norm360(19.8950 + 0.0830853001 * d)
        )
        Planet.SATURN -> Elements(
            n = norm360(113.6634 + 2.38980e-5 * d), i = 2.4886 - 1.081e-7 * d,
            w = norm360(339.3939 + 2.97661e-5 * d), a = 9.55475,
            e = 0.055546 - 9.499e-9 * d, m = norm360(316.9670 + 0.0334442282 * d)
        )
        Planet.URANUS -> Elements(
            n = norm360(74.0005 + 1.3978e-5 * d), i = 0.7733 + 1.9e-8 * d,
            w = norm360(96.6612 + 3.0565e-5 * d), a = 19.18171 - 1.55e-8 * d,
            e = 0.047318 + 7.45e-9 * d, m = norm360(142.5905 + 0.011725806 * d)
        )
        Planet.NEPTUNE -> Elements(
            n = norm360(131.7806 + 3.0173e-5 * d), i = 1.7700 - 2.55e-7 * d,
            w = norm360(272.8461 - 6.027e-6 * d), a = 30.05826 + 3.313e-8 * d,
            e = 0.008606 + 2.15e-9 * d, m = norm360(260.2471 + 0.005995147 * d)
        )
    }

    /** Corrections périodiques (perturbations mutuelles), en degrés, ajoutées à la longitude brute. */
    private fun planetPerturbation(planet: Planet, d: Double): Double {
        val mj = norm360(planetElements(Planet.JUPITER, d).m)
        val ms = norm360(planetElements(Planet.SATURN, d).m)
        val mu = norm360(planetElements(Planet.URANUS, d).m)
        return when (planet) {
            Planet.JUPITER ->
                -0.332 * sinD(2 * mj - 5 * ms - 67.6) +
                -0.056 * sinD(2 * mj - 2 * ms + 21.0) +
                0.042 * sinD(3 * mj - 5 * ms + 21.0) +
                -0.036 * sinD(mj - 2 * ms) +
                0.022 * cosD(mj - ms) +
                0.023 * sinD(2 * mj - 3 * ms + 52.0) +
                -0.016 * sinD(mj - 5 * ms - 69.0)
            Planet.SATURN ->
                0.812 * sinD(2 * mj - 5 * ms - 67.6) +
                -0.229 * cosD(2 * mj - 4 * ms - 2.0) +
                0.119 * sinD(mj - 2 * ms - 3.0) +
                0.046 * sinD(2 * mj - 6 * ms - 69.0) +
                0.014 * sinD(mj - 3 * ms + 32.0)
            Planet.URANUS ->
                0.040 * sinD(ms - 2 * mu + 6.0) +
                0.035 * sinD(ms - 3 * mu + 33.0) +
                -0.015 * sinD(mj - mu + 20.0)
            else -> 0.0
        }
    }

    fun planetLongitude(planet: Planet, jd: Double): Double {
        val d = dayNumber(jd)
        val el = planetElements(planet, d)
        val (xh, yh) = heliocentricXY(el)
        val (xs, ys) = sunGeocentricXY(d)
        val lon = norm360(atan2D(yh + ys, xh + xs))
        return norm360(lon + planetPerturbation(planet, d))
    }

    /**
     * Longitude approchée de Pluton — formule empirique héliocentrique de Schlyter,
     * utilisée telle quelle comme approximation géocentrique (la parallaxe Terre-Soleil
     * est faible à cette distance, écart max ~2°, acceptable pour ce corps mineur du chart).
     */
    fun plutoLongitude(jd: Double): Double {
        val d = dayNumber(jd)
        val s = norm360(50.03 + 0.033459652 * d)
        val p = norm360(238.95 + 0.003968789 * d)
        val lon = 238.9508 + 0.00400703 * d +
            -19.799 * sinD(p) + 19.848 * cosD(p) +
            0.897 * sinD(2 * p) - 4.956 * cosD(2 * p) +
            0.610 * sinD(3 * p) + 1.211 * cosD(3 * p) +
            -0.341 * sinD(4 * p) - 0.190 * cosD(4 * p) +
            0.128 * sinD(5 * p) - 0.034 * cosD(5 * p) +
            -0.038 * sinD(6 * p) + 0.031 * cosD(6 * p) +
            0.020 * sinD(s - p) - 0.010 * cosD(s - p)
        return norm360(lon)
    }

    /**
     * Instant (jour julien) où le Soleil était 88° d'arc avant sa position de naissance —
     * la "date de Design" en Human Design. La vitesse du Soleil (~0.9856°/jour) est quasi
     * constante, 2-3 itérations suffisent à converger sous 0.0001°.
     */
    fun findDesignJd(birthJd: Double): Double {
        val targetLon = norm360(sunLongitude(birthJd) - 88.0)
        var jd = birthJd - 88.0
        repeat(4) {
            val currentLon = sunLongitude(jd)
            var diff = targetLon - currentLon
            if (diff > 180.0) diff -= 360.0
            if (diff < -180.0) diff += 360.0
            jd += diff / 0.9856
        }
        return jd
    }
}
