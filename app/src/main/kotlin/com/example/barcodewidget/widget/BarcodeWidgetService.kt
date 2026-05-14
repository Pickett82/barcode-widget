package com.example.barcodewidget.widget

import android.content.Intent
import android.widget.RemoteViewsService

class BarcodeWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return BarcodeWidgetFactory(applicationContext)
    }
}
