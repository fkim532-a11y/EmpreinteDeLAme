package com.francekim.empreintedelame.data.repository

import android.content.Context
import com.francekim.empreintedelame.data.model.MirrorSoulContent
import com.francekim.empreintedelame.data.model.MirrorSoulEntry
import org.json.JSONObject
import java.util.Locale

class MirrorSoulRepository(private val context: Context) {

    fun load(): MirrorSoulContent {
        val lang = Locale.getDefault().language
        val fileName = if (lang == "fr") "mirror_soul_fr.json" else "mirror_soul_en.json"
        return try {
            val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val root = JSONObject(json)

            val pools = buildMap {
                root.keys().forEach { lifePathKey ->
                    val array = root.getJSONArray(lifePathKey)
                    val entries = (0 until array.length()).map { i ->
                        val obj = array.getJSONObject(i)
                        MirrorSoulEntry(
                            type  = obj.optString("type", "energie"),
                            texte = obj.optString("texte", "")
                        )
                    }
                    put(lifePathKey, entries)
                }
            }

            MirrorSoulContent(pools)
        } catch (e: Exception) {
            MirrorSoulContent(emptyMap())
        }
    }
}
