package com.example.barcodewidget

import android.content.Context
import com.example.barcodewidget.data.db.AppDatabase
import com.example.barcodewidget.data.repository.ILoyaltyCardRepository
import com.example.barcodewidget.data.repository.LoyaltyCardRepository

class AppContainer(context: Context) {
    val database: AppDatabase = AppDatabase.getInstance(context)
    val repository: ILoyaltyCardRepository = LoyaltyCardRepository(database.loyaltyCardDao())
}
