package com.francekim.empreintedelame.data.repository

import android.content.Context
import com.francekim.empreintedelame.domain.calculator.MirrorHourCalculator
import com.francekim.empreintedelame.domain.model.MirrorHourInfo
import org.json.JSONObject
import java.util.Locale

class MirrorHourRepository(private val context: Context) {

    fun load(): Map<String, MirrorHourInfo> {
        val lang     = Locale.getDefault().language
        val fileName = if (lang == "fr") "mirror_hours_fr.json" else "mirror_hours_en.json"
        return try {
            val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val root = JSONObject(json)
            buildMap {
                for (hour in MirrorHourCalculator.allHours) {
                    if (!root.has(hour)) continue
                    val obj = root.getJSONObject(hour)
                    put(hour, MirrorHourInfo(
                        hour      = hour,
                        angel     = obj.optString("angel",     ""),
                        vibration = obj.optString("vibration", ""),
                        message   = obj.optString("message",   ""),
                        advice    = obj.optString("advice",    "")
                    ))
                }
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
