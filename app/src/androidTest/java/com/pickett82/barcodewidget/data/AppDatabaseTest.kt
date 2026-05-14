package com.pickett82.barcodewidget.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: CardDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).build()
        dao = database.cardDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun orderedQuery_keepsPinnedCardsFirst() = runBlocking {
        dao.insert(
            LoyaltyCardEntity(
                canonicalStoreName = "Waitrose",
                displayStoreName = "Waitrose",
                storeInitials = "W",
                brandColor = 0,
                textColor = 0,
                barcodeValue = "1111",
                barcodeFormat = BarcodeSymbology.CODE_128.name,
                pinnedRank = null,
                usageCount = 9,
                lastUsedAt = 100,
            ),
        )
        dao.insert(
            LoyaltyCardEntity(
                canonicalStoreName = "Tesco",
                displayStoreName = "Tesco",
                storeInitials = "T",
                brandColor = 0,
                textColor = 0,
                barcodeValue = "2222",
                barcodeFormat = BarcodeSymbology.CODE_128.name,
                pinnedRank = 0,
                usageCount = 1,
                lastUsedAt = 10,
            ),
        )

        val ordered = dao.getOrderedCards()

        assertEquals("Tesco", ordered.first().displayStoreName)
    }
}
