package com.francekim.empreintedelame.data.repository

import android.content.Context
import com.francekim.empreintedelame.domain.calculator.humandesign.Center
import com.francekim.empreintedelame.domain.calculator.humandesign.HDType
import com.francekim.empreintedelame.domain.model.CompatibilityEntry
import com.francekim.empreintedelame.domain.model.CompatibilityNature
import org.json.JSONObject
import java.util.Locale

private val MASTER_NUMBERS = setOf(11, 22, 33)
private fun lifePathRank(n: Int) = if (n in MASTER_NUMBERS) 0 to n else 1 to n

private val HD_TYPE_RANK = mapOf(
    HDType.GENERATOR to 0,
    HDType.MANIFESTING_GENERATOR to 1,
    HDType.MANIFESTOR to 2,
    HDType.PROJECTOR to 3,
    HDType.REFLECTOR to 4
)

enum class CenterPairState { DEFINED_DEFINED, DEFINED_UNDEFINED, UNDEFINED_UNDEFINED }

class CompatibilityRepository(private val context: Context) {

    private fun readJson(fileName: String): JSONObject? = try {
        val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
        JSONObject(json)
    } catch (e: Exception) {
        null
    }

    private fun parseEntry(obj: JSONObject): CompatibilityEntry {
        val natureRaw = obj.optString("nature", "harmonieux")
        val nature = when (natureRaw) {
            "complementaire" -> CompatibilityNature.COMPLEMENTAIRE
            "tension"         -> CompatibilityNature.TENSION
            else              -> CompatibilityNature.HARMONIEUX
        }
        return CompatibilityEntry(
            score      = obj.optInt("score", 0),
            nature     = nature,
            love       = obj.optString("love", ""),
            friendship = obj.optString("friendship", ""),
            family     = obj.optString("family", ""),
            work       = obj.optString("work", ""),
            chemistry  = obj.optString("chemistry", "")
        )
    }

    fun lifePathCompatibility(lifePathA: Int, lifePathB: Int): CompatibilityEntry? {
        val lang     = Locale.getDefault().language
        val fileName = if (lang == "fr") "compatibility_lifepath_fr.json" else "compatibility_lifepath_en.json"
        val root = readJson(fileName) ?: return null
        val (lo, hi) = listOf(lifePathA, lifePathB).sortedBy { lifePathRank(it).let { r -> r.first * 1000 + r.second } }
        val key = "$lo-$hi"
        if (!root.has(key)) return null
        return parseEntry(root.getJSONObject(key))
    }

    fun hdTypeCompatibility(typeA: HDType, typeB: HDType): CompatibilityEntry? {
        val lang     = Locale.getDefault().language
        val fileName = if (lang == "fr") "compatibility_hdtypes_fr.json" else "compatibility_hdtypes_en.json"
        val root = readJson(fileName) ?: return null
        val (lo, hi) = listOf(typeA, typeB).sortedBy { HD_TYPE_RANK[it] ?: 99 }
        val key = "${lo.name}-${hi.name}"
        if (!root.has(key)) return null
        return parseEntry(root.getJSONObject(key))
    }

    fun centerCompatibility(center: Center, definedA: Boolean, definedB: Boolean): String? {
        val lang     = Locale.getDefault().language
        val fileName = if (lang == "fr") "compatibility_hdcenters_fr.json" else "compatibility_hdcenters_en.json"
        val root = readJson(fileName) ?: return null
        val centerObj = root.optJSONObject(center.name) ?: return null
        val state = when {
            definedA && definedB               -> CenterPairState.DEFINED_DEFINED
            !definedA && !definedB             -> CenterPairState.UNDEFINED_UNDEFINED
            else                                -> CenterPairState.DEFINED_UNDEFINED
        }
        if (!centerObj.has(state.name)) return null
        return centerObj.optString(state.name, "")
    }
}
