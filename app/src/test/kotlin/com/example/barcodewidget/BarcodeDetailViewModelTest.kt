package com.example.barcodewidget

import com.example.barcodewidget.data.db.LoyaltyCard
import com.example.barcodewidget.data.repository.ILoyaltyCardRepository
import com.example.barcodewidget.ui.viewmodel.BarcodeDetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BarcodeDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    private fun makeTestCard(id: Long) = LoyaltyCard(
        id = id,
        storeName = "Test Store",
        barcodeValue = "123456",
        barcodeFormat = "CODE_128",
        logoResName = null,
        customLogoUri = null
    )

    @Test
    fun `incrementUsage is called on ViewModel init`() = runTest {
        val card = makeTestCard(42L)
        var usageIncrementedFor: Long? = null

        val fakeRepo = object : ILoyaltyCardRepository {
            override val orderedCards: Flow<List<LoyaltyCard>> = flowOf(listOf(card))
            override fun getCardById(id: Long): Flow<LoyaltyCard?> = flowOf(card.takeIf { it.id == id })
            override suspend fun incrementUsage(id: Long) { usageIncrementedFor = id }
            override suspend fun addCard(storeName: String, barcodeValue: String, barcodeFormat: String, logoResName: String?, customLogoUri: String?): Long = 0L
            override suspend fun deleteCard(card: LoyaltyCard) {}
            override suspend fun updateCard(card: LoyaltyCard) {}
        }

        val viewModel = BarcodeDetailViewModel(fakeRepo, 42L)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(42L, usageIncrementedFor)
    }
}
