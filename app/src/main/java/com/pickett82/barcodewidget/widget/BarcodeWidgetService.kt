package com.pickett82.barcodewidget.widget

import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.pickett82.barcodewidget.BarcodeWidgetApp
import com.pickett82.barcodewidget.MainActivity
import com.pickett82.barcodewidget.R
import com.pickett82.barcodewidget.data.LoyaltyCard
import com.pickett82.barcodewidget.ui.StoreLogoBitmapFactory
import kotlinx.coroutines.runBlocking

class BarcodeWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return BarcodeRemoteViewsFactory(applicationContext as BarcodeWidgetApp)
    }
}

private class BarcodeRemoteViewsFactory(
    private val app: BarcodeWidgetApp,
) : RemoteViewsService.RemoteViewsFactory {
    private var cards: List<LoyaltyCard> = emptyList()

    override fun onCreate() = Unit

    override fun onDataSetChanged() {
        cards = runBlocking { app.cardRepository.getCardsForWidget() }
    }

    override fun onDestroy() {
        cards = emptyList()
    }

    override fun getCount(): Int = cards.size

    override fun getViewAt(position: Int): RemoteViews {
        val card = cards[position]
        val views = RemoteViews(app.packageName, R.layout.widget_card_row)
        val size = app.resources.getDimensionPixelSize(R.dimen.widget_logo_size)
        views.setImageViewBitmap(
            R.id.widget_card_logo,
            StoreLogoBitmapFactory.loadCustomLogo(app, card.customLogoUri, size)
                ?: StoreLogoBitmapFactory.createBadge(card, size),
        )
        views.setTextViewText(R.id.widget_card_store, card.displayStoreName)
        views.setTextViewText(R.id.widget_card_barcode, card.barcodeValue)
        val fillInIntent = Intent().apply {
            putExtra(MainActivity.EXTRA_CARD_ID, card.id)
        }
        views.setOnClickFillInIntent(R.id.widget_card_root, fillInIntent)
        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = cards[position].id

    override fun hasStableIds(): Boolean = true
}
