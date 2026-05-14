package com.example.barcodewidget.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LoyaltyCardDao {

    @Query(
        """
        SELECT * FROM loyalty_cards
        ORDER BY
            CASE WHEN isPinned = 1 THEN 0 ELSE 1 END ASC,
            pinOrder ASC,
            usageCount DESC,
            lastUsedAt DESC
        """
    )
    fun getOrderedCards(): Flow<List<LoyaltyCard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: LoyaltyCard): Long

    @Update
    suspend fun updateCard(card: LoyaltyCard)

    @Delete
    suspend fun deleteCard(card: LoyaltyCard)

    @Query("SELECT * FROM loyalty_cards WHERE id = :id")
    fun getCardById(id: Long): Flow<LoyaltyCard?>

    @Query("UPDATE loyalty_cards SET usageCount = usageCount + 1, lastUsedAt = :usedAt WHERE id = :id")
    suspend fun incrementUsage(id: Long, usedAt: Long)
}
