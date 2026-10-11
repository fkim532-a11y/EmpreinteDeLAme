package com.francekim.empreintedelame.domain.calculator

import java.util.Calendar
import java.util.TimeZone

// ─── Décalage UTC réel pour une naissance en France métropolitaine ─────────────
// Réutilise les données de fuseau horaire déjà intégrées à Android (tzdata IANA
// "Europe/Paris"), qui contiennent l'historique exact de tous les changements
// d'heure français, y compris les dates irrégulières fixées par décret entre
// 1976 et 1995 — bien plus fiable qu'une règle reconstituée à la main.
object FranceUtcOffsetCalculator {

    private val PARIS_ZONE: TimeZone = TimeZone.getTimeZone("Europe/Paris")

    fun calculateOffsetMinutes(
        year: Int, month: Int, day: Int,
        hour: Int, minute: Int
    ): Int {
        val calendar = Calendar.getInstance(PARIS_ZONE)
        calendar.clear()
        calendar.set(year, month - 1, day, hour, minute, 0)
        val offsetMillis = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)
        return offsetMillis / 60_000
    }
}
