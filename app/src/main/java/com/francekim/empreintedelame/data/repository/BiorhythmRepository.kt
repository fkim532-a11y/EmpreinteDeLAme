package com.francekim.empreintedelame.data.repository

import android.content.Context
import com.francekim.empreintedelame.data.model.BiorhythmContent
import org.json.JSONObject
import java.util.Locale

class BiorhythmRepository(private val context: Context) {

    fun load(): BiorhythmContent {
        val lang = Locale.getDefault().language
        val fileName = if (lang == "fr") "biorhythm_fr.json" else "biorhythm_en.json"
        return try {
            val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val root = JSONObject(json)

            val baseTexts = buildMap {
                val obj = root.getJSONObject("base")
                obj.keys().forEach { key -> put(key, obj.optString(key, "")) }
            }

            val synthesisTexts = buildMap {
                val obj = root.getJSONObject("synthesis")
                obj.keys().forEach { key -> put(key, obj.optString(key, "")) }
            }

            BiorhythmContent(baseTexts, synthesisTexts)
        } catch (e: Exception) {
            BiorhythmContent(emptyMap(), emptyMap())
        }
    }
}
