package com.example.barcodewidget

import com.example.barcodewidget.data.db.LoyaltyCard
import org.junit.Assert.assertEquals
import org.junit.Test

class LoyaltyCardOrderingTest {

    private fun createCard(
        id: Long,
        storeName: String,
        isPinned: Boolean = false,
        pinOrder: Int = 0,
        usageCount: Int = 0,
        lastUsedAt: Long = 0
    ) = LoyaltyCard(
        id = id,
        storeName = storeName,
        barcodeValue = "TEST123",
        barcodeFormat = "CODE_128",
        logoResName = null,
        customLogoUri = null,
        isPinned = isPinned,
        pinOrder = pinOrder,
        usageCount = usageCount,
        lastUsedAt = lastUsedAt
    )

    private fun sortCards(cards: List<LoyaltyCard>): List<LoyaltyCard> {
        return cards.sortedWith(
            compareBy(
                { if (it.isPinned) 0 else 1 },
                { it.pinOrder },
                { -it.usageCount },
                { -it.lastUsedAt }
            )
        )
    }

    @Test
    fun `pinned cards appear before unpinned cards`() {
        val cards = listOf(
            createCard(1, "Tesco", isPinned = false, usageCount = 100),
            createCard(2, "Boots", isPinned = true, pinOrder = 0)
        )
        val sorted = sortCards(cards)
        assertEquals("Boots", sorted[0].storeName)
        assertEquals("Tesco", sorted[1].storeName)
    }

    @Test
    fun `pinned cards ordered by pinOrder`() {
        val cards = listOf(
            createCard(1, "Tesco", isPinned = true, pinOrder = 2),
            createCard(2, "Boots", isPinned = true, pinOrder = 0),
            createCard(3, "Costa", isPinned = true, pinOrder = 1)
        )
        val sorted = sortCards(cards)
        assertEquals("Boots", sorted[0].storeName)
        assertEquals("Costa", sorted[1].storeName)
        assertEquals("Tesco", sorted[2].storeName)
    }

    @Test
    fun `unpinned cards ordered by usageCount descending`() {
        val cards = listOf(
            createCard(1, "Tesco", usageCount = 5),
            createCard(2, "Boots", usageCount = 10),
            createCard(3, "Costa", usageCount = 1)
        )
        val sorted = sortCards(cards)
        assertEquals("Boots", sorted[0].storeName)
        assertEquals("Tesco", sorted[1].storeName)
        assertEquals("Costa", sorted[2].storeName)
    }

    @Test
    fun `cards with same usage ordered by recency`() {
        val cards = listOf(
            createCard(1, "Tesco", usageCount = 5, lastUsedAt = 1000L),
            createCard(2, "Boots", usageCount = 5, lastUsedAt = 3000L),
            createCard(3, "Costa", usageCount = 5, lastUsedAt = 2000L)
        )
        val sorted = sortCards(cards)
        assertEquals("Boots", sorted[0].storeName)
        assertEquals("Costa", sorted[1].storeName)
        assertEquals("Tesco", sorted[2].storeName)
    }
}
