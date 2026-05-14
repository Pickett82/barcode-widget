package com.pickett82.barcodewidget

import android.app.Application
import com.pickett82.barcodewidget.data.AppDatabase
import com.pickett82.barcodewidget.data.CardRepository
import com.pickett82.barcodewidget.data.StoreCatalogRepository

class BarcodeWidgetApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.create(this) }
    val storeCatalogRepository: StoreCatalogRepository by lazy { StoreCatalogRepository(this) }
    val cardRepository: CardRepository by lazy {
        CardRepository(
            context = this,
            cardDao = database.cardDao(),
            storeCatalogRepository = storeCatalogRepository,
        )
    }
}
