package com.example.barcodewidget.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.barcodewidget.BarcodeWidgetApp
import com.example.barcodewidget.data.db.LoyaltyCard
import com.example.barcodewidget.ui.viewmodel.CardListViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardListScreen(
    onAddCard: () -> Unit,
    onCardTap: (Long) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as BarcodeWidgetApp
    val viewModel: CardListViewModel = viewModel(factory = CardListViewModel.Factory(app.container.repository))
    val cards by viewModel.cards.collectAsState()
    var selectedCard by remember { mutableStateOf<LoyaltyCard?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Loyalty Cards") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCard) {
                Icon(Icons.Default.Add, contentDescription = "Add card")
            }
        }
    ) { paddingValues ->
        if (cards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No loyalty cards yet.\nTap + to add one.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cards, key = { it.id }) { card ->
                    LoyaltyCardItem(
                        card = card,
                        onClick = { onCardTap(card.id) },
                        onLongClick = { selectedCard = card }
                    )
                }
            }
        }
    }

    selectedCard?.let { card ->
        CardOptionsDialog(
            card = card,
            onDismiss = { selectedCard = null },
            onTogglePin = {
                viewModel.togglePin(card)
                selectedCard = null
            },
            onDelete = {
                viewModel.deleteCard(card)
                selectedCard = null
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoyaltyCardItem(
    card: LoyaltyCard,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val logoResId = card.logoResName?.let { resName ->
                context.resources.getIdentifier(resName, "drawable", context.packageName)
            } ?: 0

            when {
                card.customLogoUri != null -> {
                    AsyncImage(
                        model = card.customLogoUri,
                        contentDescription = card.storeName,
                        modifier = Modifier.size(48.dp)
                    )
                }
                logoResId != 0 -> {
                    Image(
                        painter = painterResource(id = logoResId),
                        contentDescription = card.storeName,
                        modifier = Modifier.size(48.dp)
                    )
                }
                else -> {
                    val placeholderId = context.resources.getIdentifier(
                        "logo_placeholder", "drawable", context.packageName
                    )
                    if (placeholderId != 0) {
                        Image(
                            painter = painterResource(id = placeholderId),
                            contentDescription = card.storeName,
                            modifier = Modifier.size(48.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                card.storeName.firstOrNull()?.toString() ?: "?",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.storeName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (card.lastUsedAt > 0) {
                    val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                        .format(Date(card.lastUsedAt))
                    Text(
                        text = "Last used: $dateStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (card.isPinned) {
                Icon(
                    Icons.Default.PushPin,
                    contentDescription = "Pinned",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CardOptionsDialog(
    card: LoyaltyCard,
    onDismiss: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(card.storeName) },
        text = {
            Column {
                TextButton(onClick = onTogglePin, modifier = Modifier.fillMaxWidth()) {
                    Text(if (card.isPinned) "Unpin card" else "Pin card")
                }
                TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) {
                    Text("Delete card", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
