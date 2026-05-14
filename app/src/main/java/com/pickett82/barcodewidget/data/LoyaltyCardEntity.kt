package com.pickett82.barcodewidget.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loyalty_cards")
data class LoyaltyCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "canonical_store_name") val canonicalStoreName: String,
    @ColumnInfo(name = "display_store_name") val displayStoreName: String,
    @ColumnInfo(name = "store_initials") val storeInitials: String,
    @ColumnInfo(name = "brand_color") val brandColor: Int,
    @ColumnInfo(name = "text_color") val textColor: Int,
    @ColumnInfo(name = "custom_logo_uri") val customLogoUri: String? = null,
    @ColumnInfo(name = "barcode_value") val barcodeValue: String,
    @ColumnInfo(name = "barcode_format") val barcodeFormat: String,
    @ColumnInfo(name = "pinned_rank") val pinnedRank: Int? = null,
    @ColumnInfo(name = "usage_count") val usageCount: Int = 0,
    @ColumnInfo(name = "last_used_at") val lastUsedAt: Long = 0,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at") val updatedAt: Long = createdAt,
)
