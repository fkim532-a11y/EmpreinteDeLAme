package com.francekim.empreintedelame.data.repository

import android.content.Context
import com.francekim.empreintedelame.domain.calculator.Arrow
import com.francekim.empreintedelame.domain.model.ArrowInterpretation
import com.francekim.empreintedelame.domain.model.NumberInterpretation
import org.json.JSONObject
import java.util.Locale

class InterpretationRepository(private val context: Context) {

    fun load(): Map<Int, NumberInterpretation> {
        val lang     = Locale.getDefault().language
        val fileName = if (lang == "fr") "interpretations_fr.json" else "interpretations_en.json"
        return try {
            val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val root = JSONObject(json)
            buildMap {
                for (key in root.keys()) {
                    val n   = key.toIntOrNull() ?: continue
                    val obj = root.getJSONObject(key)
                    put(n, NumberInterpretation(
                        keyword    = obj.optString("keyword",    ""),
                        lifePath   = obj.optString("lifePath",   ""),
                        shadow     = obj.optString("shadow",     ""),
                        maturity   = obj.optString("maturity",   ""),
                        summit     = obj.optString("summit",     ""),
                        expression = obj.optString("expression", ""),
                        soul       = obj.optString("soul",       ""),
                        intimate   = obj.optString("intimate",   ""),
                        heritage   = obj.optString("heritage",   ""),
                        action     = obj.optString("action",     ""),
                        bridge     = obj.optString("bridge",     ""),
                        missing    = obj.optString("missing",    ""),
                        overload   = obj.optString("overload",   ""),
                        missingLifePath = obj.optString("missingLifePath", ""),
                        innate     = obj.optString("innate",     ""),
                        year       = obj.optString("year",       ""),
                        month      = obj.optString("month",      ""),
                        day        = obj.optString("day",        ""),
                        universalYear = obj.optString("universalYear", ""),
                        yearChallenge = obj.optString("yearChallenge", ""),
                        unconscious   = obj.optString("unconscious",   ""),
                        trimestre1     = obj.optString("trimestre1",     ""),
                        trimestre2     = obj.optString("trimestre2",     ""),
                        trimestre3     = obj.optString("trimestre3",     ""),
                        realization    = obj.optString("realization",    ""),
                        monthChallenge = obj.optString("monthChallenge", "")
                    ))
                }
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    // Deuxième lecture (spirituelle) du chiffre 8 — clé JSON "8s", distincte de la lecture matérielle "8".
    fun loadEightSpiritual(): NumberInterpretation? {
        val lang     = Locale.getDefault().language
        val fileName = if (lang == "fr") "interpretations_fr.json" else "interpretations_en.json"
        return try {
            val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val root = JSONObject(json)
            if (!root.has("8s")) return null
            val obj = root.getJSONObject("8s")
            NumberInterpretation(
                keyword        = obj.optString("keyword",        ""),
                lifePath       = obj.optString("lifePath",       ""),
                shadow         = obj.optString("shadow",         ""),
                maturity       = obj.optString("maturity",       ""),
                summit         = obj.optString("summit",         ""),
                expression     = obj.optString("expression",     ""),
                soul           = obj.optString("soul",           ""),
                intimate       = obj.optString("intimate",       ""),
                heritage       = obj.optString("heritage",       ""),
                action         = obj.optString("action",         ""),
                bridge         = obj.optString("bridge",         ""),
                year           = obj.optString("year",           ""),
                month          = obj.optString("month",          ""),
                day            = obj.optString("day",            ""),
                yearChallenge  = obj.optString("yearChallenge",  ""),
                realization    = obj.optString("realization",    ""),
                monthChallenge = obj.optString("monthChallenge", ""),
                universalYear  = obj.optString("universalYear",  ""),
                unconscious    = obj.optString("unconscious",    ""),
                missingLifePath = obj.optString("missingLifePath", ""),
                innate          = obj.optString("innate",          "")
            )
        } catch (e: Exception) {
            null
        }
    }

    // Flèches de Pythagore — fichier séparé, une entrée par flèche (pleine + vide).
    fun loadArrows(): Map<Arrow, ArrowInterpretation> {
        val lang     = Locale.getDefault().language
        val fileName = if (lang == "fr") "arrows_fr.json" else "arrows_en.json"
        return try {
            val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val root = JSONObject(json)
            buildMap {
                for (arrow in Arrow.entries) {
                    if (!root.has(arrow.name)) continue
                    val obj = root.getJSONObject(arrow.name)
                    put(arrow, ArrowInterpretation(
                        full  = obj.optString("full",  ""),
                        empty = obj.optString("empty", "")
                    ))
                }
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    // Transit de la lettre — une entrée par lettre A-Z, indépendante des chiffres.
    fun loadLetterTransits(): Map<Char, String> {
        val lang     = Locale.getDefault().language
        val fileName = if (lang == "fr") "letters_fr.json" else "letters_en.json"
        return try {
            val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val root = JSONObject(json)
            buildMap {
                for (key in root.keys()) {
                    val letter = key.firstOrNull() ?: continue
                    put(letter, root.optString(key, ""))
                }
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
