package com.pickett82.barcodewidget.data

import android.content.Context
import androidx.core.net.toUri
import com.pickett82.barcodewidget.widget.BarcodeWidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Locale

data class LoyaltyCard(
    val id: Long,
    val canonicalStoreName: String,
    val displayStoreName: String,
    val storeInitials: String,
    val brandColor: Int,
    val textColor: Int,
    val customLogoUri: String?,
    val barcodeValue: String,
    val barcodeFormat: BarcodeSymbology,
    val pinnedRank: Int?,
    val usageCount: Int,
    val lastUsedAt: Long,
    val createdAt: Long,
    val updatedAt: Long,
)

enum class BarcodeSymbology {
    CODE_128,
    EAN_13,
    QR_CODE,
    UPC_A,
    UPC_E,
    ITF,
    AZTEC,
    PDF_417,
}

data class CardDraft(
    val storeName: String,
    val barcodeValue: String,
    val barcodeFormat: BarcodeSymbology,
    val isCustomStore: Boolean,
    val customLogoUri: String? = null,
)

class CardRepository(
    private val context: Context,
    private val cardDao: CardDao,
    private val storeCatalogRepository: StoreCatalogRepository,
) {
    fun observeCards(): Flow<List<LoyaltyCardEntity>> = cardDao.observeOrderedCards()

    suspend fun getCardsForWidget(): List<LoyaltyCard> = cardDao.getOrderedCards().map { it.toModel() }

    suspend fun getCard(id: Long): LoyaltyCard? = cardDao.getCardById(id)?.toModel()

    suspend fun saveCard(draft: CardDraft): Result<Long> = withContext(Dispatchers.IO) {
        runCatching {
            val now = System.currentTimeMillis()
            val knownStore = storeCatalogRepository.findKnownStore(draft.storeName)
            val resolvedStoreName = if (draft.isCustomStore) {
                draft.storeName.trim()
            } else {
                knownStore?.canonicalName ?: draft.storeName.trim()
            }
            require(resolvedStoreName.isNotBlank()) { "Choose a store before saving." }
            require(draft.barcodeValue.isNotBlank()) { "Enter or scan a barcode value first." }

            val defaults = knownStore ?: StoreCatalogEntry(
                canonicalName = resolvedStoreName,
                aliases = emptyList(),
                initials = resolvedStoreName
                    .split(Regex("\\s+"))
                    .filter { it.isNotBlank() }
                    .take(2)
                    .joinToString("") { it.first().uppercase(Locale.UK) }
                    .ifBlank { "C" },
                brandColor = 0xFF455A64.toInt(),
                textColor = 0xFFFFFFFF.toInt(),
            )

            val customLogoUri = draft.customLogoUri?.takeIf { it.isNotBlank() }?.also { uri ->
                context.contentResolver.takePersistableUriPermission(
                    uri.toUri(),
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }

            val entity = LoyaltyCardEntity(
                canonicalStoreName = defaults.canonicalName,
                displayStoreName = resolvedStoreName,
                storeInitials = defaults.initials,
                brandColor = defaults.brandColor,
                textColor = defaults.textColor,
                customLogoUri = customLogoUri,
                barcodeValue = draft.barcodeValue.trim(),
                barcodeFormat = draft.barcodeFormat.name,
                createdAt = now,
                updatedAt = now,
            )
            cardDao.insert(entity).also { BarcodeWidgetProvider.requestRefresh(context) }
        }
    }

    suspend fun deleteCard(id: Long) = withContext(Dispatchers.IO) {
        cardDao.getCardById(id)?.let {
            cardDao.delete(it)
            BarcodeWidgetProvider.requestRefresh(context)
        }
    }

    suspend fun incrementUsage(id: Long) = withContext(Dispatchers.IO) {
        val current = cardDao.getCardById(id) ?: return@withContext
        cardDao.update(
            current.copy(
                usageCount = current.usageCount + 1,
                lastUsedAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            ),
        )
        BarcodeWidgetProvider.requestRefresh(context)
    }

    suspend fun togglePinned(id: Long) = withContext(Dispatchers.IO) {
        val current = cardDao.getCardById(id) ?: return@withContext
        val updated = if (current.pinnedRank == null) {
            current.copy(
                pinnedRank = cardDao.getMaxPinnedRank() + 1,
                updatedAt = System.currentTimeMillis(),
            )
        } else {
            current.copy(
                pinnedRank = null,
                updatedAt = System.currentTimeMillis(),
            )
        }
        cardDao.update(updated)
        normalizePinnedRanks()
    }

    suspend fun movePinnedCard(id: Long, direction: Int) = withContext(Dispatchers.IO) {
        val pinned = cardDao.getPinnedCards().toMutableList()
        val currentIndex = pinned.indexOfFirst { it.id == id }
        if (currentIndex == -1) return@withContext
        val targetIndex = (currentIndex + direction).coerceIn(0, pinned.lastIndex)
        if (currentIndex == targetIndex) return@withContext
        val card = pinned.removeAt(currentIndex)
        pinned.add(targetIndex, card)
        val now = System.currentTimeMillis()
        cardDao.updateAll(
            pinned.mapIndexed { index, item ->
                item.copy(pinnedRank = index, updatedAt = now)
            },
        )
        BarcodeWidgetProvider.requestRefresh(context)
    }

    suspend fun normalizePinnedRanks() = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val pinned = cardDao.getPinnedCards()
        cardDao.updateAll(
            pinned.mapIndexed { index, item ->
                item.copy(pinnedRank = index, updatedAt = now)
            },
        )
        BarcodeWidgetProvider.requestRefresh(context)
    }

    private fun LoyaltyCardEntity.toModel(): LoyaltyCard {
        return LoyaltyCard(
            id = id,
            canonicalStoreName = canonicalStoreName,
            displayStoreName = displayStoreName,
            storeInitials = storeInitials,
            brandColor = brandColor,
            textColor = textColor,
            customLogoUri = customLogoUri,
            barcodeValue = barcodeValue,
            barcodeFormat = BarcodeSymbology.valueOf(barcodeFormat),
            pinnedRank = pinnedRank,
            usageCount = usageCount,
            lastUsedAt = lastUsedAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }
}
