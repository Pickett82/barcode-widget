package com.example.barcodewidget.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.barcodewidget.BarcodeWidgetApp
import com.example.barcodewidget.R
import com.example.barcodewidget.data.db.LoyaltyCard
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class BarcodeWidgetFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var cards: List<LoyaltyCard> = emptyList()

    override fun onCreate() {
        loadData()
    }

    override fun onDataSetChanged() {
        loadData()
    }

    private fun loadData() {
        val app = context.applicationContext as BarcodeWidgetApp
        cards = runBlocking {
            app.container.repository.orderedCards.first()
        }
    }

    override fun onDestroy() {
        cards = emptyList()
    }

    override fun getCount(): Int = cards.size

    override fun getViewAt(position: Int): RemoteViews {
        val card = cards.getOrNull(position)
            ?: return RemoteViews(context.packageName, R.layout.widget_item)

        val views = RemoteViews(context.packageName, R.layout.widget_item)
        views.setTextViewText(R.id.widget_store_name, card.storeName)

        val logoResId = card.logoResName?.let { resName ->
            context.resources.getIdentifier(resName, "drawable", context.packageName)
        } ?: 0

        if (logoResId != 0) {
            views.setImageViewResource(R.id.widget_logo, logoResId)
        } else {
            val placeholderId = context.resources.getIdentifier(
                "logo_placeholder", "drawable", context.packageName
            )
            if (placeholderId != 0) {
                views.setImageViewResource(R.id.widget_logo, placeholderId)
            }
        }

        val fillInIntent = Intent().apply {
            putExtra("card_id", card.id)
        }
        views.setOnClickFillInIntent(R.id.widget_item_root, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long =
        cards.getOrNull(position)?.id ?: position.toLong()

    override fun hasStableIds(): Boolean = true
}
