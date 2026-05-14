package com.pickett82.barcodewidget.data

import org.junit.Assert.assertEquals
import org.junit.Test

class CardOrderingTest {
    @Test
    fun pinnedCardsSortBeforeUsageAndRecency() {
        val cards = listOf(
            LoyaltyCardEntity(
                id = 1,
                canonicalStoreName = "A",
                displayStoreName = "A",
                storeInitials = "A",
                brandColor = 0,
                textColor = 0,
                barcodeValue = "111",
                barcodeFormat = BarcodeSymbology.CODE_128.name,
                pinnedRank = null,
                usageCount = 99,
                lastUsedAt = 500,
            ),
            LoyaltyCardEntity(
                id = 2,
                canonicalStoreName = "B",
                displayStoreName = "B",
                storeInitials = "B",
                brandColor = 0,
                textColor = 0,
                barcodeValue = "222",
                barcodeFormat = BarcodeSymbology.CODE_128.name,
                pinnedRank = 0,
                usageCount = 1,
                lastUsedAt = 10,
            ),
            LoyaltyCardEntity(
                id = 3,
                canonicalStoreName = "C",
                displayStoreName = "C",
                storeInitials = "C",
                brandColor = 0,
                textColor = 0,
                barcodeValue = "333",
                barcodeFormat = BarcodeSymbology.CODE_128.name,
                pinnedRank = null,
                usageCount = 40,
                lastUsedAt = 700,
            ),
        )

        val ordered = cards.sortedWith(
            compareBy<LoyaltyCardEntity> { it.pinnedRank == null }
                .thenBy { it.pinnedRank ?: Int.MAX_VALUE }
                .thenByDescending { it.usageCount }
                .thenByDescending { it.lastUsedAt }
                .thenByDescending { it.updatedAt },
        )

        assertEquals(listOf(2L, 1L, 3L), ordered.map { it.id })
    }
}
