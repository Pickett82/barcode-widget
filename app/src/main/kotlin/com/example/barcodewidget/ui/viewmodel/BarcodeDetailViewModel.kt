package com.example.barcodewidget.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.barcodewidget.data.db.LoyaltyCard
import com.example.barcodewidget.data.repository.ILoyaltyCardRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BarcodeDetailViewModel(
    private val repository: ILoyaltyCardRepository,
    private val cardId: Long
) : ViewModel() {

    val card: StateFlow<LoyaltyCard?> = repository.getCardById(cardId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            repository.incrementUsage(cardId)
        }
    }

    class Factory(
        private val repository: ILoyaltyCardRepository,
        private val cardId: Long
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return BarcodeDetailViewModel(repository, cardId) as T
        }
    }
}
