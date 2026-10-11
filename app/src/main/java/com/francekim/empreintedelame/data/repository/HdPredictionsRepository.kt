package com.francekim.empreintedelame.data.repository

import android.content.Context
import com.francekim.empreintedelame.data.model.HdGateReadingInfo
import com.francekim.empreintedelame.data.model.HdLifeCycleInfo
import com.francekim.empreintedelame.data.model.HdPersonalMonthInfo
import com.francekim.empreintedelame.data.model.HdPersonalYearInfo
import com.francekim.empreintedelame.data.model.HdPredictionsContent
import org.json.JSONObject
import java.util.Locale

class HdPredictionsRepository(private val context: Context) {

    fun load(): HdPredictionsContent {
        val lang = Locale.getDefault().language
        val fileName = if (lang == "fr") "predictions_hd_fr.json" else "predictions_hd_en.json"
        return try {
            val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val root = JSONObject(json)

            val cycles = buildMap {
                val obj = root.getJSONObject("cycles")
                obj.keys().forEach { key ->
                    val c = obj.getJSONObject(key)
                    put(key, HdLifeCycleInfo(
                        name        = c.optString("name", ""),
                        theme       = c.optString("theme", ""),
                        description = c.optString("description", ""),
                        invitation  = c.optString("invitation", "")
                    ))
                }
            }

            val personalYears = buildMap {
                val obj = root.getJSONObject("personalYears")
                obj.keys().forEach { key ->
                    val y = obj.getJSONObject(key)
                    put(key.toInt(), HdPersonalYearInfo(
                        theme       = y.optString("theme", ""),
                        hdTheme     = y.optString("hdTheme", ""),
                        description = y.optString("description", ""),
                        strategy    = y.optString("strategy", ""),
                        affirmation = y.optString("affirmation", "")
                    ))
                }
            }

            val personalMonths = buildMap {
                val obj = root.getJSONObject("personalMonths")
                obj.keys().forEach { key ->
                    val m = obj.getJSONObject(key)
                    put(key.toInt(), HdPersonalMonthInfo(
                        theme        = m.optString("theme", ""),
                        hdFocus      = m.optString("hdFocus", ""),
                        description  = m.optString("description", ""),
                        practicalTip = m.optString("practicalTip", "")
                    ))
                }
            }

            val dailyGates = buildMap {
                val obj = root.getJSONObject("dailyGates")
                obj.keys().forEach { key ->
                    val g = obj.getJSONObject(key)
                    put(key.toInt(), HdGateReadingInfo(
                        yiKingName  = g.optString("yiKingName", ""),
                        description = g.optString("description", ""),
                        invitation  = g.optString("invitation", "")
                    ))
                }
            }

            HdPredictionsContent(cycles, personalYears, personalMonths, dailyGates)
        } catch (e: Exception) {
            HdPredictionsContent(emptyMap(), emptyMap(), emptyMap(), emptyMap())
        }
    }
}
