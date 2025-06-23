// com.clinicaapp.ui.viewmodel.PurchaseViewModelFactory.kt
package com.clinicaapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.clinicaapp.data.repository.PurchaseRepository

class PurchaseViewModelFactory(private val purchaseRepository: PurchaseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PurchaseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PurchaseViewModel(purchaseRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}