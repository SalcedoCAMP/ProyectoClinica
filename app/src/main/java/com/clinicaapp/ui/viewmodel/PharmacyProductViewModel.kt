// com.clinicaapp.ui.viewmodel.PharmacyProductViewModel.kt
package com.clinicaapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clinicaapp.data.db.entities.PharmacyProduct
import com.clinicaapp.data.repository.PharmacyProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PharmacyProductViewModel(
    private val productRepository: PharmacyProductRepository
) : ViewModel() {

    private val _allProducts = MutableStateFlow<List<PharmacyProduct>>(emptyList())
    val allProducts: StateFlow<List<PharmacyProduct>> = _allProducts.asStateFlow()

    private val _currentProduct = MutableStateFlow<PharmacyProduct?>(null)
    val currentProduct: StateFlow<PharmacyProduct?> = _currentProduct.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        viewModelScope.launch {
            _searchQuery.collectLatest { query ->
                if (query.isBlank()) {
                    productRepository.getAllProducts().collectLatest {
                        _allProducts.value = it
                    }
                } else {
                    productRepository.searchProducts(query).collectLatest {
                        _allProducts.value = it
                    }
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun getProductById(productId: Long) {
        viewModelScope.launch {
            _currentProduct.value = productRepository.getProductById(productId)
        }
    }

    fun saveProduct(product: PharmacyProduct) {
        viewModelScope.launch {
            productRepository.saveProduct(product)
        }
    }
    fun insertProduct(product: PharmacyProduct) = viewModelScope.launch {
        productRepository.insertProduct(product)
    }

    fun updateProduct(product: PharmacyProduct) {
        viewModelScope.launch {
            productRepository.updateProduct(product)
        }
    }

    fun deleteProduct(product: PharmacyProduct) {
        viewModelScope.launch {
            productRepository.deleteProduct(product)
        }
    }

    // Limpia el producto actual al salir de la pantalla de edición
    fun clearCurrentProduct() {
        _currentProduct.value = null
    }
}