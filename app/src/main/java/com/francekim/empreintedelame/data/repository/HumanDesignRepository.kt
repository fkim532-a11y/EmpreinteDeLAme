package com.francekim.empreintedelame.data.repository

import android.content.Context
import com.francekim.empreintedelame.data.model.HumanDesignAuthorityInfo
import com.francekim.empreintedelame.data.model.HumanDesignCenterInfo
import com.francekim.empreintedelame.data.model.HumanDesignChannelInfo
import com.francekim.empreintedelame.data.model.HumanDesignContent
import com.francekim.empreintedelame.data.model.HumanDesignDefinitionInfo
import com.francekim.empreintedelame.data.model.HumanDesignGateInfo
import com.francekim.empreintedelame.data.model.HumanDesignProfileInfo
import com.francekim.empreintedelame.data.model.HumanDesignTypeInfo
import org.json.JSONObject
import java.util.Locale

class HumanDesignRepository(private val context: Context) {

    fun load(): HumanDesignContent {
        val lang = Locale.getDefault().language
        val fileName = if (lang == "fr") "human_design_fr.json" else "human_design_en.json"
        return try {
            val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val root = JSONObject(json)

            val types = buildMap {
                val obj = root.getJSONObject("types")
                obj.keys().forEach { key ->
                    val t = obj.getJSONObject(key)
                    put(key, HumanDesignTypeInfo(
                        name         = t.optString("name", ""),
                        keyword      = t.optString("keyword", ""),
                        strategy     = t.optString("strategy", ""),
                        notSelfTheme = t.optString("notSelfTheme", ""),
                        description  = t.optString("description", "")
                    ))
                }
            }

            val authorities = buildMap {
                val obj = root.getJSONObject("authorities")
                obj.keys().forEach { key ->
                    val a = obj.getJSONObject(key)
                    put(key, HumanDesignAuthorityInfo(
                        name        = a.optString("name", ""),
                        keyword     = a.optString("keyword", ""),
                        description = a.optString("description", "")
                    ))
                }
            }

            val profiles = buildMap {
                val obj = root.getJSONObject("profiles")
                obj.keys().forEach { key ->
                    val p = obj.getJSONObject(key)
                    put(key, HumanDesignProfileInfo(
                        name        = p.optString("name", ""),
                        description = p.optString("description", "")
                    ))
                }
            }

            val definitions = buildMap {
                val obj = root.getJSONObject("definitions")
                obj.keys().forEach { key ->
                    val d = obj.getJSONObject(key)
                    put(key, HumanDesignDefinitionInfo(
                        name        = d.optString("name", ""),
                        description = d.optString("description", "")
                    ))
                }
            }

            val centers = buildMap {
                val obj = root.getJSONObject("centers")
                obj.keys().forEach { key ->
                    val c = obj.getJSONObject(key)
                    put(key, HumanDesignCenterInfo(
                        name                  = c.optString("name", ""),
                        definedDescription    = c.optString("definedDescription", ""),
                        undefinedDescription  = c.optString("undefinedDescription", "")
                    ))
                }
            }

            val channels = buildMap {
                val obj = root.getJSONObject("channels")
                obj.keys().forEach { key ->
                    val c = obj.getJSONObject(key)
                    put(key, HumanDesignChannelInfo(
                        name        = c.optString("name", ""),
                        keyword     = c.optString("keyword", ""),
                        description = c.optString("description", "")
                    ))
                }
            }

            val gates = buildMap {
                val obj = root.getJSONObject("gates")
                obj.keys().forEach { key ->
                    val g = obj.getJSONObject(key)
                    put(key.toInt(), HumanDesignGateInfo(
                        name        = g.optString("name", ""),
                        keyword     = g.optString("keyword", ""),
                        description = g.optString("description", "")
                    ))
                }
            }

            HumanDesignContent(types, authorities, profiles, definitions, centers, channels, gates)
        } catch (e: Exception) {
            HumanDesignContent(emptyMap(), emptyMap(), emptyMap(), emptyMap(), emptyMap(), emptyMap(), emptyMap())
        }
    }
}
