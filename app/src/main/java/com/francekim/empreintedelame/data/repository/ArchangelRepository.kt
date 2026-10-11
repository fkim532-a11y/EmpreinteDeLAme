package com.francekim.empreintedelame.data.repository

import android.content.Context
import com.francekim.empreintedelame.domain.calculator.Archangel
import com.francekim.empreintedelame.domain.model.ArchangelInfo
import org.json.JSONObject
import java.util.Locale

class ArchangelRepository(private val context: Context) {

    fun load(): Map<Archangel, ArchangelInfo> {
        val lang     = Locale.getDefault().language
        val fileName = if (lang == "fr") "archangels_fr.json" else "archangels_en.json"
        return try {
            val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val root = JSONObject(json)
            buildMap {
                for (archangel in Archangel.entries) {
                    if (!root.has(archangel.key)) continue
                    val obj = root.getJSONObject(archangel.key)
                    put(archangel, ArchangelInfo(
                        name        = obj.optString("name",        ""),
                        keyword     = obj.optString("keyword",     ""),
                        domain      = obj.optString("domain",      ""),
                        description = obj.optString("description", ""),
                        invocation  = obj.optString("invocation",  "")
                    ))
                }
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
