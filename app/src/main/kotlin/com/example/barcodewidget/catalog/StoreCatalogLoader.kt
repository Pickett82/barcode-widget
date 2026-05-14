package com.example.barcodewidget.catalog

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object StoreCatalogLoader {

    private var entries: List<StoreEntry> = emptyList()

    fun init(context: Context) {
        val json = context.assets.open("store_catalog.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<StoreEntryJson>>() {}.type
        val rawEntries: List<StoreEntryJson> = Gson().fromJson(json, type)
        entries = rawEntries.map {
            StoreEntry(
                canonicalName = it.canonicalName,
                aliases = it.aliases,
                logoResName = it.logoResName
            )
        }
    }

    fun findByAlias(input: String): StoreEntry? {
        val normalized = input.trim().lowercase()
        return entries.firstOrNull { entry ->
            entry.aliases.any { alias -> alias.lowercase() == normalized }
        }
    }

    fun allStores(): List<StoreEntry> = entries.sortedBy { it.canonicalName }

    private data class StoreEntryJson(
        val canonicalName: String,
        val aliases: List<String>,
        val logoResName: String
    )
}
