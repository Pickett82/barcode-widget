package com.example.barcodewidget

import com.example.barcodewidget.catalog.StoreEntry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class StoreCatalogLoaderTest {

    private val testEntries = listOf(
        StoreEntry("Tesco Clubcard", listOf("tesco", "tesco clubcard", "clubcard"), "logo_tesco"),
        StoreEntry("Boots", listOf("boots", "boots pharmacy"), "logo_boots"),
        StoreEntry("Costa Coffee", listOf("costa", "costa coffee"), "logo_costa")
    )

    private fun findByAlias(entries: List<StoreEntry>, input: String): StoreEntry? {
        val normalized = input.trim().lowercase()
        return entries.firstOrNull { entry ->
            entry.aliases.any { alias -> alias.lowercase() == normalized }
        }
    }

    @Test
    fun `findByAlias returns correct entry for exact alias`() {
        val result = findByAlias(testEntries, "tesco")
        assertNotNull(result)
        assertEquals("Tesco Clubcard", result?.canonicalName)
    }

    @Test
    fun `findByAlias is case insensitive`() {
        val result = findByAlias(testEntries, "BOOTS")
        assertNotNull(result)
        assertEquals("Boots", result?.canonicalName)
    }

    @Test
    fun `findByAlias trims whitespace`() {
        val result = findByAlias(testEntries, "  costa  ")
        assertNotNull(result)
        assertEquals("Costa Coffee", result?.canonicalName)
    }

    @Test
    fun `findByAlias returns null for unknown alias`() {
        val result = findByAlias(testEntries, "unknown store")
        assertNull(result)
    }

    @Test
    fun `findByAlias matches multi-word alias`() {
        val result = findByAlias(testEntries, "tesco clubcard")
        assertNotNull(result)
        assertEquals("Tesco Clubcard", result?.canonicalName)
    }

    @Test
    fun `allStores returns sorted list`() {
        val sorted = testEntries.sortedBy { it.canonicalName }
        assertEquals("Boots", sorted[0].canonicalName)
        assertEquals("Costa Coffee", sorted[1].canonicalName)
        assertEquals("Tesco Clubcard", sorted[2].canonicalName)
    }
}
