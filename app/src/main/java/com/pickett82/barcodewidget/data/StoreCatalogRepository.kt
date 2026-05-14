package com.pickett82.barcodewidget.data

import android.content.Context
import android.graphics.Color
import org.json.JSONArray
import java.util.Locale

data class StoreCatalogEntry(
    val canonicalName: String,
    val aliases: List<String>,
    val initials: String,
    val brandColor: Int,
    val textColor: Int,
)

class StoreCatalogRepository(
    private val context: Context,
) {
    private val entries: List<StoreCatalogEntry> by lazy {
        parseCatalog(
            context.assets.open("store_catalog.json").bufferedReader().use { it.readText() },
        )
    }

    fun allStores(): List<StoreCatalogEntry> = entries

    fun findKnownStore(input: String): StoreCatalogEntry? {
        val normalized = normalizeStoreKey(input)
        return entries.firstOrNull { entry ->
            normalizeStoreKey(entry.canonicalName) == normalized ||
                entry.aliases.any { normalizeStoreKey(it) == normalized }
        }
    }

    fun filter(query: String): List<StoreCatalogEntry> {
        val normalized = normalizeStoreKey(query)
        if (normalized.isBlank()) {
            return entries
        }
        return entries.filter { entry ->
            normalizeStoreKey(entry.canonicalName).contains(normalized) ||
                entry.aliases.any { normalizeStoreKey(it).contains(normalized) }
        }
    }

    companion object {
        fun parseCatalog(json: String): List<StoreCatalogEntry> {
            val array = JSONArray(json)
            return buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    val aliasesJson = item.getJSONArray("aliases")
                    add(
                        StoreCatalogEntry(
                            canonicalName = item.getString("canonicalName"),
                            aliases = List(aliasesJson.length(), aliasesJson::getString),
                            initials = item.getString("initials"),
                            brandColor = Color.parseColor(item.getString("brandColor")),
                            textColor = Color.parseColor(item.getString("textColor")),
                        ),
                    )
                }
            }
        }

        fun normalizeStoreKey(value: String): String {
            return value
                .trim()
                .lowercase(Locale.UK)
                .replace(Regex("[^a-z0-9]"), "")
        }
    }
}
