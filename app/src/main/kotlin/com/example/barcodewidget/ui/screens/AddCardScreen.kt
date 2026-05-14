package com.example.barcodewidget.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.barcodewidget.BarcodeWidgetApp
import com.example.barcodewidget.catalog.StoreEntry
import com.example.barcodewidget.ui.viewmodel.AddCardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardScreen(
    onNavigateBack: () -> Unit,
    onCardSaved: (Long) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as BarcodeWidgetApp
    val viewModel: AddCardViewModel = viewModel(factory = AddCardViewModel.Factory(app.container.repository))
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.savedCardId) {
        uiState.savedCardId?.let { id -> onCardSaved(id) }
    }

    var formatMenuExpanded by remember { mutableStateOf(false) }
    var showStoreSelector by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.updateCustomLogoUri(uri?.toString())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Loyalty Card") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (showStoreSelector) {
            StoreSelectionScreen(
                searchQuery = uiState.storeSearchQuery,
                onSearchQueryChange = viewModel::updateStoreSearchQuery,
                stores = viewModel.filteredStores(),
                selectedStore = uiState.selectedStore,
                isCustomSelected = uiState.isCustomStore,
                onStoreSelected = { store ->
                    viewModel.selectStore(store)
                    showStoreSelector = false
                },
                onCustomSelected = {
                    viewModel.selectCustomStore()
                    showStoreSelector = false
                },
                onBack = { showStoreSelector = false }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Step 1: Enter Barcode", style = MaterialTheme.typography.titleMedium)

                OutlinedTextField(
                    value = uiState.barcodeValue,
                    onValueChange = viewModel::updateBarcodeValue,
                    label = { Text("Barcode Value") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = formatMenuExpanded,
                    onExpandedChange = { formatMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = uiState.barcodeFormat,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Barcode Format") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formatMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = formatMenuExpanded,
                        onDismissRequest = { formatMenuExpanded = false }
                    ) {
                        viewModel.availableFormats.forEach { format ->
                            DropdownMenuItem(
                                text = { Text(format) },
                                onClick = {
                                    viewModel.updateBarcodeFormat(format)
                                    formatMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Step 2: Choose Store", style = MaterialTheme.typography.titleMedium)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showStoreSelector = true },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val displayText = when {
                            uiState.isCustomStore -> "Custom store: ${uiState.customStoreName.ifEmpty { "(enter name below)" }}"
                            uiState.selectedStore != null -> uiState.selectedStore.canonicalName
                            else -> "Tap to select store"
                        }
                        Text(
                            text = displayText,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                if (uiState.isCustomStore) {
                    OutlinedTextField(
                        value = uiState.customStoreName,
                        onValueChange = viewModel::updateCustomStoreName,
                        label = { Text("Custom Store Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (uiState.customLogoUri != null) {
                        AsyncImage(
                            model = uiState.customLogoUri,
                            contentDescription = "Custom logo",
                            modifier = Modifier.size(80.dp)
                        )
                    }

                    TextButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Text(if (uiState.customLogoUri == null) "Add Custom Logo (Optional)" else "Change Logo")
                    }
                }

                uiState.error?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Button(
                    onClick = viewModel::saveCard,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (uiState.isSaving) "Saving..." else "Save Card")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreSelectionScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    stores: List<StoreEntry>,
    selectedStore: StoreEntry?,
    isCustomSelected: Boolean,
    onStoreSelected: (StoreEntry) -> Unit,
    onCustomSelected: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Choose Store") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            label = { Text("Search stores") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true
        )

        LazyColumn {
            item {
                StoreListItem(
                    name = "Custom Store",
                    isSelected = isCustomSelected,
                    onClick = onCustomSelected
                )
            }
            items(stores) { store ->
                StoreListItem(
                    name = store.canonicalName,
                    isSelected = selectedStore == store,
                    onClick = { onStoreSelected(store) }
                )
            }
        }
    }
}

@Composable
fun StoreListItem(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, modifier = Modifier.weight(1f))
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
