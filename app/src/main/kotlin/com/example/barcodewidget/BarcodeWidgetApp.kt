package com.example.barcodewidget

import android.app.Application
import com.example.barcodewidget.catalog.StoreCatalogLoader

class BarcodeWidgetApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        StoreCatalogLoader.init(this)
        container = AppContainer(this)
    }
}
