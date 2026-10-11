package com.francekim.empreintedelame.data.repository

import android.content.Context
import com.francekim.empreintedelame.data.model.YiKingHexagram
import com.francekim.empreintedelame.data.model.YiKingThemes
import org.json.JSONObject
import java.util.Locale

class YiKingRepository(private val context: Context) {

    // Contenu disponible en FR et EN ; le repli sur le fichier FR ne sert que si
    // le fichier de la langue courante est introuvable.
    fun load(): Map<Int, YiKingHexagram> {
        val lang = Locale.getDefault().language
        val fileName = if (lang == "fr") "yi_king_fr.json" else "yi_king_en.json"
        return try {
            val json = try {
                context.assets.open(fileName).bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                context.assets.open("yi_king_fr.json").bufferedReader().use { it.readText() }
            }
            val root = JSONObject(json)
            val array = root.getJSONArray("hexagrammes")
            buildMap {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val themesObj = obj.optJSONObject("themes") ?: JSONObject()
                    val themes = YiKingThemes(
                        amour     = themesObj.optString("amour", ""),
                        finance   = themesObj.optString("finance", ""),
                        sante     = themesObj.optString("sante", ""),
                        spirituel = themesObj.optString("spirituel", ""),
                        famille   = themesObj.optString("famille", "")
                    )
                    val traitsObj = obj.optJSONObject("traits") ?: JSONObject()
                    val traits = buildMap {
                        traitsObj.keys().forEach { key ->
                            put(key.toInt(), traitsObj.optString(key, ""))
                        }
                    }
                    val numero = obj.optInt("numero")
                    put(numero, YiKingHexagram(
                        numero          = numero,
                        nom             = obj.optString("nom", ""),
                        nomChinois      = obj.optString("nom_chinois", ""),
                        trigrammeHaut   = obj.optString("trigramme_haut", ""),
                        trigrammeBas    = obj.optString("trigramme_bas", ""),
                        image           = obj.optString("image", ""),
                        jugementGeneral = obj.optString("jugement_general", ""),
                        themes          = themes,
                        traits          = traits
                    ))
                }
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
