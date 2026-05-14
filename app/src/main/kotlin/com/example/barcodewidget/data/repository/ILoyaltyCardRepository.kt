package com.example.barcodewidget.data.repository

import com.example.barcodewidget.data.db.LoyaltyCard
import kotlinx.coroutines.flow.Flow

interface ILoyaltyCardRepository {
    val orderedCards: Flow<List<LoyaltyCard>>
    suspend fun addCard(
        storeName: String,
        barcodeValue: String,
        barcodeFormat: String,
        logoResName: String?,
        customLogoUri: String?
    ): Long
    suspend fun deleteCard(card: LoyaltyCard)
    suspend fun updateCard(card: LoyaltyCard)
    suspend fun incrementUsage(id: Long)
    fun getCardById(id: Long): Flow<LoyaltyCard?>
}
