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

class CardListViewModel(private val repository: ILoyaltyCardRepository) : ViewModel() {

    val cards: StateFlow<List<LoyaltyCard>> = repository.orderedCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun togglePin(card: LoyaltyCard) {
        viewModelScope.launch {
            val updatedCard = if (card.isPinned) {
                card.copy(isPinned = false, pinOrder = 0)
            } else {
                val currentPinned = cards.value.filter { it.isPinned }
                val nextPinOrder = (currentPinned.maxOfOrNull { it.pinOrder } ?: -1) + 1
                card.copy(isPinned = true, pinOrder = nextPinOrder)
            }
            repository.updateCard(updatedCard)
        }
    }

    fun deleteCard(card: LoyaltyCard) {
        viewModelScope.launch {
            repository.deleteCard(card)
        }
    }

    class Factory(private val repository: ILoyaltyCardRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return CardListViewModel(repository) as T
        }
    }
}
