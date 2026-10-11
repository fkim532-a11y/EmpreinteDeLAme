package com.francekim.empreintedelame.data.repository

import android.content.Context
import com.francekim.empreintedelame.data.model.ArchangelCard
import com.francekim.empreintedelame.data.model.ArchangelCardTheme
import org.json.JSONArray
import java.util.Locale

class ArchangelCardRepository(private val context: Context) {

    private val themeKeys = listOf("spirituel", "amour", "travail", "sante", "famille")

    fun load(): List<ArchangelCard> {
        val lang     = Locale.getDefault().language
        val fileName = if (lang == "fr") "archangel_cards_fr.json" else "archangel_cards_en.json"
        return try {
            val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val array = JSONArray(json)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val themes = buildMap {
                        for (key in themeKeys) {
                            val themeObj = obj.optJSONObject(key) ?: continue
                            put(key, ArchangelCardTheme(
                                situation = themeObj.optString("situation", ""),
                                conseil   = themeObj.optString("conseil", "")
                            ))
                        }
                    }
                    add(ArchangelCard(
                        id      = obj.optInt("id"),
                        name    = obj.optString("name", ""),
                        keyword = obj.optString("keyword", ""),
                        image   = obj.optString("image", ""),
                        themes  = themes
                    ))
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
