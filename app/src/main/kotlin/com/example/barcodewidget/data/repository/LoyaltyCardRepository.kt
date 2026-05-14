package com.example.barcodewidget.data.repository

import com.example.barcodewidget.data.db.LoyaltyCard
import com.example.barcodewidget.data.db.LoyaltyCardDao
import kotlinx.coroutines.flow.Flow

class LoyaltyCardRepository(private val dao: LoyaltyCardDao) : ILoyaltyCardRepository {

    override val orderedCards: Flow<List<LoyaltyCard>> = dao.getOrderedCards()

    override suspend fun addCard(
        storeName: String,
        barcodeValue: String,
        barcodeFormat: String,
        logoResName: String?,
        customLogoUri: String?
    ): Long {
        val card = LoyaltyCard(
            storeName = storeName,
            barcodeValue = barcodeValue,
            barcodeFormat = barcodeFormat,
            logoResName = logoResName,
            customLogoUri = customLogoUri
        )
        return dao.insertCard(card)
    }

    override suspend fun deleteCard(card: LoyaltyCard) = dao.deleteCard(card)

    override suspend fun updateCard(card: LoyaltyCard) = dao.updateCard(card)

    override suspend fun incrementUsage(id: Long) = dao.incrementUsage(id, System.currentTimeMillis())

    override fun getCardById(id: Long): Flow<LoyaltyCard?> = dao.getCardById(id)
}
