package com.francekim.empreintedelame.domain.calculator

import java.time.LocalTime

object MirrorHourCalculator {

    /** Les 24 heures miroirs classiques : "00:00", "01:01" … "23:23". */
    val doubleHours: List<String> = (0..23).map { h -> "%02d:%02d".format(h, h) }

    /** Les heures miroirs inversées : minutes = heure inversée (ex. "12:21", "13:31"). */
    val reversedHours: List<String> = (0..23).mapNotNull { h ->
        val hh = "%02d".format(h)
        val mm = hh.reversed()
        val m  = mm.toInt()
        if (m in 0..59 && mm != hh) "$hh:$mm" else null
    }

    /** Les heures triples : les 3 derniers chiffres identiques (ex. "01:11", "15:55").
     *  "00:00", "11:11" et "22:22" en sont exclues, elles figurent déjà dans [doubleHours]. */
    val tripleHours: List<String> = (0..23).mapNotNull { h ->
        val d = h % 10
        val label = "%02d:%d%d".format(h, d, d)
        if (d <= 5 && label !in doubleHours) label else null
    }

    /** Toutes les heures spéciales confondues, pour le calcul de l'heure la plus proche. */
    val allHours: List<String> = doubleHours + reversedHours + tripleHours

    /** Heure miroir la plus proche de [now], en tenant compte du passage minuit ↔ 23:xx. */
    fun nearestHour(now: LocalTime = LocalTime.now()): String {
        val nowMinutes = now.hour * 60 + now.minute
        return allHours.minBy { hourLabel ->
            val h = hourLabel.substring(0, 2).toInt()
            val m = hourLabel.substring(3, 5).toInt()
            val hourMinutes = h * 60 + m
            val diff = kotlin.math.abs(nowMinutes - hourMinutes)
            minOf(diff, 1440 - diff)
        }
    }
}
