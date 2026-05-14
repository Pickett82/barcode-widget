package com.example.barcodewidget.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loyalty_cards")
data class LoyaltyCard(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val storeName: String,
    val barcodeValue: String,
    val barcodeFormat: String,
    val logoResName: String?,
    val customLogoUri: String?,
    val isPinned: Boolean = false,
    val pinOrder: Int = 0,
    val usageCount: Int = 0,
    val lastUsedAt: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)
