package com.pickett82.barcodewidget.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query(
        """
        SELECT * FROM loyalty_cards
        ORDER BY
            CASE WHEN pinned_rank IS NULL THEN 1 ELSE 0 END ASC,
            pinned_rank ASC,
            usage_count DESC,
            last_used_at DESC,
            updated_at DESC
        """,
    )
    fun observeOrderedCards(): Flow<List<LoyaltyCardEntity>>

    @Query(
        """
        SELECT * FROM loyalty_cards
        ORDER BY
            CASE WHEN pinned_rank IS NULL THEN 1 ELSE 0 END ASC,
            pinned_rank ASC,
            usage_count DESC,
            last_used_at DESC,
            updated_at DESC
        """,
    )
    suspend fun getOrderedCards(): List<LoyaltyCardEntity>

    @Query("SELECT * FROM loyalty_cards WHERE id = :id LIMIT 1")
    suspend fun getCardById(id: Long): LoyaltyCardEntity?

    @Query("SELECT * FROM loyalty_cards WHERE pinned_rank IS NOT NULL ORDER BY pinned_rank ASC")
    suspend fun getPinnedCards(): List<LoyaltyCardEntity>

    @Query("SELECT COALESCE(MAX(pinned_rank), -1) FROM loyalty_cards")
    suspend fun getMaxPinnedRank(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: LoyaltyCardEntity): Long

    @Update
    suspend fun update(card: LoyaltyCardEntity)

    @Update
    suspend fun updateAll(cards: List<LoyaltyCardEntity>)

    @Delete
    suspend fun delete(card: LoyaltyCardEntity)
}
