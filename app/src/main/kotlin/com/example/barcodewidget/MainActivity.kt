package com.example.barcodewidget

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.barcodewidget.ui.navigation.AppNavigation
import com.example.barcodewidget.ui.theme.BarcodeWidgetTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val cardId = intent?.getLongExtra("card_id", -1L).takeIf { it != -1L }
        setContent {
            BarcodeWidgetTheme {
                AppNavigation(initialCardId = cardId)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
