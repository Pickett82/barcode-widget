package com.pickett82.barcodewidget

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.activity.result.contract.ActivityResultContracts
import com.pickett82.barcodewidget.data.BarcodeSymbology
import com.pickett82.barcodewidget.data.LoyaltyCard
import com.pickett82.barcodewidget.data.StoreCatalogEntry
import com.pickett82.barcodewidget.ui.BarcodeBitmapFactory
import com.pickett82.barcodewidget.ui.BarcodeWidgetUiState
import com.pickett82.barcodewidget.ui.BarcodeWidgetViewModel
import com.pickett82.barcodewidget.ui.ScreenState
import com.pickett82.barcodewidget.ui.StoreLogoBitmapFactory
import com.pickett82.barcodewidget.ui.ScannerScreen
import com.pickett82.barcodewidget.ui.theme.BarcodeWidgetTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<BarcodeWidgetViewModel> {
        val app = application as BarcodeWidgetApp
        BarcodeWidgetViewModel.Factory(app.cardRepository, app.storeCatalogRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        setContent {
            BarcodeWidgetTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    BarcodeWidgetApp(viewModel = viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val cardId = intent?.getLongExtra(EXTRA_CARD_ID, -1L) ?: -1L
        if (cardId > 0) {
            viewModel.onWidgetOpen(cardId)
        }
    }

    companion object {
        const val EXTRA_CARD_ID = "extra_card_id"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BarcodeWidgetApp(
    viewModel: BarcodeWidgetViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.form.errorMessage) {
        uiState.form.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (uiState.currentScreen) {
                            ScreenState.Home -> "Barcode Widget"
                            ScreenState.AddCard -> "Add loyalty card"
                            ScreenState.Scanner -> "Scan barcode"
                            is ScreenState.Detail -> "Barcode"
                        },
                    )
                },
                navigationIcon = {
                    if (uiState.currentScreen != ScreenState.Home) {
                        IconButton(onClick = viewModel::onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (uiState.currentScreen == ScreenState.Home) {
                FloatingActionButton(onClick = viewModel::showAddCard) {
                    Icon(Icons.Filled.Add, contentDescription = "Add card")
                }
            }
        },
    ) { paddingValues ->
        when (val screen = uiState.currentScreen) {
            ScreenState.Home -> HomeScreen(
                uiState = uiState,
                modifier = Modifier.padding(paddingValues),
                onOpenCard = viewModel::openDetail,
                onTogglePinned = viewModel::togglePinned,
                onMovePinned = viewModel::movePinned,
            )
            ScreenState.AddCard -> AddCardScreen(
                uiState = uiState,
                modifier = Modifier.padding(paddingValues),
                onStoreQueryChange = viewModel::updateStoreQuery,
                onSelectStore = viewModel::selectKnownStore,
                onToggleCustomStore = viewModel::toggleCustomStore,
                onCustomStoreChange = viewModel::updateCustomStoreName,
                onBarcodeValueChange = viewModel::updateBarcodeValue,
                onBarcodeFormatChange = viewModel::updateBarcodeFormat,
                onShowScanner = viewModel::showScanner,
                onCustomLogoSelected = viewModel::updateCustomLogo,
                onSave = viewModel::saveCard,
            )
            ScreenState.Scanner -> ScannerScreen(
                modifier = Modifier.padding(paddingValues),
                onBarcodeScanned = viewModel::onBarcodeScanned,
                onCancel = viewModel::onBack,
            )
            is ScreenState.Detail -> DetailScreen(
                card = uiState.cards.firstOrNull { it.id == screen.cardId },
                modifier = Modifier.padding(paddingValues),
                onDelete = { viewModel.deleteCard(screen.cardId) },
            )
        }
    }
}

@Composable
private fun HomeScreen(
    uiState: BarcodeWidgetUiState,
    modifier: Modifier = Modifier,
    onOpenCard: (Long) -> Unit,
    onTogglePinned: (Long) -> Unit,
    onMovePinned: (Long, Int) -> Unit,
) {
    if (uiState.cards.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Add a loyalty card to see it here and in the home-screen widget.",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(uiState.cards, key = LoyaltyCard::id) { card ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenCard(card.id) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CardLogo(card = card, modifier = Modifier.size(56.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = card.displayStoreName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Barcode • ${card.barcodeValue.takeLast(6)}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        if (card.pinnedRank != null) {
                            Text(
                                text = "Pinned position ${card.pinnedRank + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        IconButton(onClick = { onTogglePinned(card.id) }) {
                            Icon(Icons.Filled.PushPin, contentDescription = "Toggle pinned")
                        }
                        if (card.pinnedRank != null) {
                            Row {
                                IconButton(onClick = { onMovePinned(card.id, -1) }) {
                                    Icon(Icons.Filled.ArrowUpward, contentDescription = "Move up")
                                }
                                IconButton(onClick = { onMovePinned(card.id, 1) }) {
                                    Icon(Icons.Filled.ArrowDownward, contentDescription = "Move down")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddCardScreen(
    uiState: BarcodeWidgetUiState,
    modifier: Modifier = Modifier,
    onStoreQueryChange: (String) -> Unit,
    onSelectStore: (StoreCatalogEntry) -> Unit,
    onToggleCustomStore: (Boolean) -> Unit,
    onCustomStoreChange: (String) -> Unit,
    onBarcodeValueChange: (String) -> Unit,
    onBarcodeFormatChange: (BarcodeSymbology) -> Unit,
    onShowScanner: () -> Unit,
    onCustomLogoSelected: (String?) -> Unit,
    onSave: () -> Unit,
) {
    val form = uiState.form
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        onCustomLogoSelected(uri?.toString())
    }
    val scrollState = rememberScrollState()
    var formatMenuExpanded by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilterChip(
                selected = !form.useCustomStore,
                onClick = { onToggleCustomStore(false) },
                label = { Text("Known store") },
            )
            FilterChip(
                selected = form.useCustomStore,
                onClick = { onToggleCustomStore(true) },
                label = { Text("Custom store") },
            )
        }

        if (form.useCustomStore) {
            OutlinedTextField(
                value = form.customStoreName,
                onValueChange = onCustomStoreChange,
                label = { Text("Store name") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedButton(onClick = { launcher.launch(arrayOf("image/*")) }) {
                Icon(Icons.Filled.Upload, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text(if (form.customLogoUri == null) "Choose optional custom logo" else "Replace custom logo")
            }
        } else {
            OutlinedTextField(
                value = form.storeQuery,
                onValueChange = onStoreQueryChange,
                label = { Text("Search known stores") },
                modifier = Modifier.fillMaxWidth(),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                uiState.stores.take(12).forEach { store ->
                    FilterChip(
                        selected = form.selectedKnownStore?.canonicalName == store.canonicalName,
                        onClick = { onSelectStore(store) },
                        label = { Text(store.canonicalName) },
                    )
                }
            }
        }

        OutlinedTextField(
            value = form.barcodeValue,
            onValueChange = onBarcodeValueChange,
            label = { Text("Barcode value") },
            modifier = Modifier.fillMaxWidth(),
            supportingText = { Text("Type a barcode number or scan one with the camera.") },
        )

        Box {
            OutlinedButton(onClick = { formatMenuExpanded = true }) {
                Text("Format: ${form.barcodeFormat.name.replace('_', ' ')}")
            }
            androidx.compose.material3.DropdownMenu(
                expanded = formatMenuExpanded,
                onDismissRequest = { formatMenuExpanded = false },
            ) {
                BarcodeSymbology.entries.forEach { format ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(format.name.replace('_', ' ')) },
                        onClick = {
                            onBarcodeFormatChange(format)
                            formatMenuExpanded = false
                        },
                    )
                }
            }
        }

        OutlinedButton(onClick = onShowScanner, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.CameraAlt, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text("Scan barcode")
        }

        Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
            Text("Save loyalty card")
        }
    }
}

@Composable
private fun DetailScreen(
    card: LoyaltyCard?,
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
) {
    if (card == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Card not found.")
        }
        return
    }

    val configuration = LocalConfiguration.current
    val context = LocalContext.current
    val barcodeBitmap by produceState<android.graphics.Bitmap?>(
        initialValue = null,
        key1 = card.id,
        key2 = configuration.screenWidthDp,
    ) {
        value = BarcodeBitmapFactory.create(
            value = card.barcodeValue,
            format = card.barcodeFormat,
            width = (configuration.screenWidthDp * context.resources.displayMetrics.density).toInt() - 64,
            height = (220 * context.resources.displayMetrics.density).toInt(),
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        CardLogo(card = card, modifier = Modifier.size(84.dp))
        Text(text = card.displayStoreName, style = MaterialTheme.typography.headlineSmall)
        barcodeBitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "Barcode for ${card.displayStoreName}",
                modifier = Modifier
                    .fillMaxWidth()
                    .background(androidx.compose.ui.graphics.Color.White, RoundedCornerShape(20.dp))
                    .padding(16.dp),
                contentScale = ContentScale.FillWidth,
            )
        }
        Text(text = card.barcodeValue, style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Opens instantly offline and stays synced to the widget from local data.",
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text("Delete card")
        }
    }
}

@Composable
private fun CardLogo(
    card: LoyaltyCard,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val targetSize = with(context.resources.displayMetrics) { (56 * density).toInt() }
    val bitmap by produceState<android.graphics.Bitmap?>(
        initialValue = null,
        key1 = card.id,
        key2 = card.customLogoUri,
    ) {
        value = StoreLogoBitmapFactory.loadCustomLogo(context, card.customLogoUri, targetSize)
            ?: StoreLogoBitmapFactory.createBadge(card, targetSize)
    }
    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = "${card.displayStoreName} logo",
            modifier = modifier.clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop,
        )
    } ?: Box(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}
