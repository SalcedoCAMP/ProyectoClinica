// com.clinicaapp.ui.viewmodel.PurchaseViewModel.kt
package com.clinicaapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clinicaapp.data.db.entities.PurchaseWithItems
import com.clinicaapp.data.repository.PurchaseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PurchaseViewModel(private val purchaseRepository: PurchaseRepository) : ViewModel() {

    val allPurchasesWithItems: StateFlow<List<PurchaseWithItems>> =
        purchaseRepository.getAllPurchasesWithItems()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )


}