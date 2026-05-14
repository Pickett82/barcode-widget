package com.pickett82.barcodewidget.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pickett82.barcodewidget.data.BarcodeSymbology
import com.pickett82.barcodewidget.data.CardDraft
import com.pickett82.barcodewidget.data.CardRepository
import com.pickett82.barcodewidget.data.LoyaltyCard
import com.pickett82.barcodewidget.data.StoreCatalogEntry
import com.pickett82.barcodewidget.data.StoreCatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ScreenState {
    data object Home : ScreenState
    data object AddCard : ScreenState
    data object Scanner : ScreenState
    data class Detail(val cardId: Long) : ScreenState
}

data class AddCardFormState(
    val barcodeValue: String = "",
    val barcodeFormat: BarcodeSymbology = BarcodeSymbology.CODE_128,
    val storeQuery: String = "",
    val selectedKnownStore: StoreCatalogEntry? = null,
    val useCustomStore: Boolean = false,
    val customStoreName: String = "",
    val customLogoUri: String? = null,
    val errorMessage: String? = null,
)

data class BarcodeWidgetUiState(
    val cards: List<LoyaltyCard> = emptyList(),
    val stores: List<StoreCatalogEntry> = emptyList(),
    val currentScreen: ScreenState = ScreenState.Home,
    val form: AddCardFormState = AddCardFormState(),
)

class BarcodeWidgetViewModel(
    private val cardRepository: CardRepository,
    private val storeCatalogRepository: StoreCatalogRepository,
) : ViewModel() {
    private val screen = MutableStateFlow<ScreenState>(ScreenState.Home)
    private val formState = MutableStateFlow(AddCardFormState())
    private val cardOpenedFromScreen = mutableSetOf<Long>()

    val uiState: StateFlow<BarcodeWidgetUiState> = combine(
        cardRepository.observeCards(),
        screen,
        formState,
    ) { cards, currentScreen, currentForm ->
        BarcodeWidgetUiState(
            cards = cards.map {
                LoyaltyCard(
                    id = it.id,
                    canonicalStoreName = it.canonicalStoreName,
                    displayStoreName = it.displayStoreName,
                    storeInitials = it.storeInitials,
                    brandColor = it.brandColor,
                    textColor = it.textColor,
                    customLogoUri = it.customLogoUri,
                    barcodeValue = it.barcodeValue,
                    barcodeFormat = BarcodeSymbology.valueOf(it.barcodeFormat),
                    pinnedRank = it.pinnedRank,
                    usageCount = it.usageCount,
                    lastUsedAt = it.lastUsedAt,
                    createdAt = it.createdAt,
                    updatedAt = it.updatedAt,
                )
            },
            stores = storeCatalogRepository.filter(currentForm.storeQuery),
            currentScreen = currentScreen,
            form = currentForm,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BarcodeWidgetUiState(stores = storeCatalogRepository.allStores()),
    )

    fun showAddCard() {
        formState.value = AddCardFormState()
        screen.value = ScreenState.AddCard
    }

    fun showScanner() {
        screen.value = ScreenState.Scanner
    }

    fun onBack() {
        screen.value = ScreenState.Home
        formState.update { it.copy(errorMessage = null) }
    }

    fun openDetail(cardId: Long) {
        screen.value = ScreenState.Detail(cardId)
        if (cardOpenedFromScreen.add(cardId)) {
            viewModelScope.launch { cardRepository.incrementUsage(cardId) }
        }
    }

    fun onWidgetOpen(cardId: Long) {
        openDetail(cardId)
    }

    fun updateStoreQuery(value: String) {
        formState.update {
            it.copy(
                storeQuery = value,
                selectedKnownStore = if (
                    it.selectedKnownStore?.canonicalName?.equals(value, ignoreCase = true) == true
                ) {
                    it.selectedKnownStore
                } else {
                    null
                },
                errorMessage = null,
            )
        }
    }

    fun selectKnownStore(entry: StoreCatalogEntry) {
        formState.update {
            it.copy(
                storeQuery = entry.canonicalName,
                selectedKnownStore = entry,
                useCustomStore = false,
                customStoreName = "",
                customLogoUri = null,
                errorMessage = null,
            )
        }
    }

    fun toggleCustomStore(enabled: Boolean) {
        formState.update {
            it.copy(
                useCustomStore = enabled,
                selectedKnownStore = if (enabled) null else it.selectedKnownStore,
                errorMessage = null,
            )
        }
    }

    fun updateCustomStoreName(value: String) {
        formState.update { it.copy(customStoreName = value, errorMessage = null) }
    }

    fun updateBarcodeValue(value: String) {
        formState.update { it.copy(barcodeValue = value, errorMessage = null) }
    }

    fun updateBarcodeFormat(format: BarcodeSymbology) {
        formState.update { it.copy(barcodeFormat = format) }
    }

    fun updateCustomLogo(uri: String?) {
        formState.update { it.copy(customLogoUri = uri) }
    }

    fun onBarcodeScanned(value: String, format: BarcodeSymbology) {
        formState.update {
            it.copy(
                barcodeValue = value,
                barcodeFormat = format,
                errorMessage = null,
            )
        }
        screen.value = ScreenState.AddCard
    }

    fun saveCard() {
        val snapshot = formState.value
        val storeName = if (snapshot.useCustomStore) snapshot.customStoreName else snapshot.storeQuery

        viewModelScope.launch {
            val result = cardRepository.saveCard(
                CardDraft(
                    storeName = storeName,
                    barcodeValue = snapshot.barcodeValue,
                    barcodeFormat = snapshot.barcodeFormat,
                    isCustomStore = snapshot.useCustomStore,
                    customLogoUri = snapshot.customLogoUri,
                ),
            )
            result.onSuccess {
                cardOpenedFromScreen.clear()
                formState.value = AddCardFormState()
                screen.value = ScreenState.Home
            }.onFailure { error ->
                formState.update { it.copy(errorMessage = error.message ?: "Unable to save card.") }
            }
        }
    }

    fun togglePinned(cardId: Long) {
        viewModelScope.launch {
            cardRepository.togglePinned(cardId)
            cardOpenedFromScreen.remove(cardId)
        }
    }

    fun movePinned(cardId: Long, direction: Int) {
        viewModelScope.launch { cardRepository.movePinnedCard(cardId, direction) }
    }

    fun deleteCard(cardId: Long) {
        viewModelScope.launch {
            cardRepository.deleteCard(cardId)
            screen.value = ScreenState.Home
            cardOpenedFromScreen.remove(cardId)
        }
    }

    class Factory(
        private val cardRepository: CardRepository,
        private val storeCatalogRepository: StoreCatalogRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BarcodeWidgetViewModel(cardRepository, storeCatalogRepository) as T
        }
    }
}
