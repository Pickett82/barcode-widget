package com.example.barcodewidget.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.barcodewidget.catalog.StoreEntry
import com.example.barcodewidget.catalog.StoreCatalogLoader
import com.example.barcodewidget.data.repository.ILoyaltyCardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddCardUiState(
    val barcodeValue: String = "",
    val barcodeFormat: String = "CODE_128",
    val selectedStore: StoreEntry? = null,
    val customStoreName: String = "",
    val customLogoUri: String? = null,
    val isCustomStore: Boolean = false,
    val storeSearchQuery: String = "",
    val isSaving: Boolean = false,
    val savedCardId: Long? = null,
    val error: String? = null
)

class AddCardViewModel(private val repository: ILoyaltyCardRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AddCardUiState())
    val uiState: StateFlow<AddCardUiState> = _uiState.asStateFlow()

    val availableFormats = listOf(
        "CODE_128", "EAN_13", "EAN_8", "QR_CODE",
        "DATA_MATRIX", "PDF_417", "AZTEC", "CODE_39", "ITF"
    )

    fun updateBarcodeValue(value: String) {
        _uiState.value = _uiState.value.copy(barcodeValue = value)
    }

    fun updateBarcodeFormat(format: String) {
        _uiState.value = _uiState.value.copy(barcodeFormat = format)
    }

    fun selectStore(store: StoreEntry) {
        _uiState.value = _uiState.value.copy(
            selectedStore = store,
            isCustomStore = false,
            customStoreName = ""
        )
    }

    fun selectCustomStore() {
        _uiState.value = _uiState.value.copy(
            selectedStore = null,
            isCustomStore = true
        )
    }

    fun updateCustomStoreName(name: String) {
        _uiState.value = _uiState.value.copy(customStoreName = name)
    }

    fun updateCustomLogoUri(uri: String?) {
        _uiState.value = _uiState.value.copy(customLogoUri = uri)
    }

    fun updateStoreSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(storeSearchQuery = query)
    }

    fun filteredStores(): List<StoreEntry> {
        val query = _uiState.value.storeSearchQuery.trim().lowercase()
        return if (query.isEmpty()) {
            StoreCatalogLoader.allStores()
        } else {
            StoreCatalogLoader.allStores().filter {
                it.canonicalName.lowercase().contains(query) ||
                    it.aliases.any { alias -> alias.lowercase().contains(query) }
            }
        }
    }

    fun saveCard() {
        val state = _uiState.value
        if (state.barcodeValue.isBlank()) {
            _uiState.value = state.copy(error = "Barcode value cannot be empty")
            return
        }
        val storeName = when {
            state.isCustomStore -> state.customStoreName.trim()
            state.selectedStore != null -> state.selectedStore.canonicalName
            else -> ""
        }
        if (storeName.isBlank()) {
            _uiState.value = state.copy(error = "Please select or enter a store name")
            return
        }
        val logoResName = if (!state.isCustomStore) state.selectedStore?.logoResName else null
        val customLogoUri = if (state.isCustomStore) state.customLogoUri else null

        _uiState.value = state.copy(isSaving = true, error = null)
        viewModelScope.launch {
            try {
                val id = repository.addCard(
                    storeName = storeName,
                    barcodeValue = state.barcodeValue,
                    barcodeFormat = state.barcodeFormat,
                    logoResName = logoResName,
                    customLogoUri = customLogoUri
                )
                _uiState.value = _uiState.value.copy(isSaving = false, savedCardId = id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, error = e.message)
            }
        }
    }

    class Factory(private val repository: ILoyaltyCardRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AddCardViewModel(repository) as T
        }
    }
}
