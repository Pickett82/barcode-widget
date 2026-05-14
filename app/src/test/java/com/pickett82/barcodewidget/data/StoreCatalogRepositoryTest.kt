package com.pickett82.barcodewidget.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class StoreCatalogRepositoryTest {
    @Test
    fun normalizeStoreKey_removesWhitespaceAndPunctuation() {
        assertEquals("waitrosepartners", StoreCatalogRepository.normalizeStoreKey(" Waitrose & Partners "))
    }

    @Test
    fun parseCatalog_readsAliasesAndBranding() {
        val json = """
            [
              {
                "canonicalName": "Tesco Clubcard",
                "aliases": ["tesco", "clubcard"],
                "initials": "TC",
                "brandColor": "#00539F",
                "textColor": "#FFFFFF"
              }
            ]
        """.trimIndent()

        val entry = StoreCatalogRepository.parseCatalog(json).single()

        assertEquals("Tesco Clubcard", entry.canonicalName)
        assertEquals(listOf("tesco", "clubcard"), entry.aliases)
        assertNotNull(entry.brandColor)
    }
}
