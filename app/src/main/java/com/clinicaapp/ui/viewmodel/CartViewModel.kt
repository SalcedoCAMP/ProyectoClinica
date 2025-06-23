// com.clinicaapp.ui.viewmodel.CartViewModel.kt
package com.clinicaapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clinicaapp.data.db.entities.PharmacyProduct
import com.clinicaapp.data.db.entities.Purchase
import com.clinicaapp.data.db.entities.PurchaseItem
import com.clinicaapp.data.db.entities.PurchaseWithItems
import com.clinicaapp.data.repository.PharmacyProductRepository
import com.clinicaapp.data.repository.PurchaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Clase de datos para representar un elemento en el carrito
data class CartItem(
    val product: PharmacyProduct,
    var quantity: Int
) {
    val subtotal: Double
        get() = product.price * quantity
}

class CartViewModel(
    private val purchaseRepository: PurchaseRepository,
    private val pharmacyProductRepository: PharmacyProductRepository // Para actualizar el stock
) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _totalAmount = MutableStateFlow(0.0)
    val totalAmount: StateFlow<Double> = _totalAmount.asStateFlow()

    private val _paidAmount = MutableStateFlow("")
    val paidAmount: StateFlow<String> = _paidAmount.asStateFlow()

    private val _changeAmount = MutableStateFlow(0.0)
    val changeAmount: StateFlow<Double> = _changeAmount.asStateFlow()

    private val _paymentMessage = MutableStateFlow<String?>(null)
    val paymentMessage: StateFlow<String?> = _paymentMessage.asStateFlow()

    // Para vouchers del administrador
    private val _allPurchases = MutableStateFlow<List<PurchaseWithItems>>(emptyList())
    val allPurchases: StateFlow<List<PurchaseWithItems>> = _allPurchases.asStateFlow()

    init {
        // Observa los cambios en el carrito para recalcular el total
        viewModelScope.launch {
            _cartItems.collect { items ->
                _totalAmount.value = items.sumOf { it.subtotal }
                calculateChange()
            }
        }
        // Carga todas las compras para el admin al iniciar
        viewModelScope.launch {
            purchaseRepository.getAllPurchasesWithItems().collect {
                _allPurchases.value = it
            }
        }
    }

    fun addProductToCart(product: PharmacyProduct, quantity: Int = 1) {
        _cartItems.update { currentItems ->
            val existingItem = currentItems.find { it.product.id == product.id }
            if (existingItem != null) {
                // Si el producto ya está en el carrito, actualiza la cantidad
                currentItems.map {
                    if (it.product.id == product.id) {
                        it.copy(quantity = it.quantity + quantity).also { updated ->
                            // Asegurarse de no exceder el stock disponible
                            if (updated.quantity > product.stock) {
                                _paymentMessage.value = "No hay suficiente stock para ${product.name}. Solo quedan ${product.stock} unidades."
                                updated.quantity = product.stock // Limitar a stock disponible
                            }
                        }
                    } else it
                }
            } else {
                // Si es un producto nuevo, añádelo
                currentItems + CartItem(product, quantity).also { newItem ->
                    if (newItem.quantity > product.stock) {
                        _paymentMessage.value = "No hay suficiente stock para ${product.name}. Solo quedan ${product.stock} unidades."
                        newItem.quantity = product.stock // Limitar a stock disponible
                    }
                }
            }
        }
    }


    fun updateCartItemQuantity(product: PharmacyProduct, newQuantity: Int) {
        _cartItems.update { currentItems ->
            currentItems.mapNotNull {
                if (it.product.id == product.id) {
                    if (newQuantity > 0) {
                        if (newQuantity <= product.stock) {
                            it.copy(quantity = newQuantity)
                        } else {
                            _paymentMessage.value = "No hay suficiente stock para ${product.name}. Solo quedan ${product.stock} unidades."
                            it.copy(quantity = product.stock) // Limitar a stock disponible
                        }
                    } else {
                        // Si la cantidad es 0 o menos, eliminar el producto
                        null
                    }
                } else {
                    it
                }
            }
        }
    }

    fun removeProductFromCart(product: PharmacyProduct) {
        _cartItems.update { currentItems ->
            currentItems.filter { it.product.id != product.id }
        }
    }

    fun setPaidAmount(amount: String) {
        _paidAmount.value = amount
        calculateChange()
    }

    private fun calculateChange() {
        val total = _totalAmount.value
        val paid = _paidAmount.value.toDoubleOrNull() ?: 0.0
        _changeAmount.value = if (paid >= total) paid - total else 0.0
    }

    fun processPayment(userId: Long): Boolean {
        val total = _totalAmount.value
        val paid = _paidAmount.value.toDoubleOrNull() ?: 0.0

        if (paid < total) {
            _paymentMessage.value = "Monto insuficiente. Faltan S/. ${String.format(Locale.getDefault(), "%.2f", total - paid)}"
            return false
        }

        if (_cartItems.value.isEmpty()) {
            _paymentMessage.value = "El carrito está vacío. Agregue productos antes de pagar."
            return false
        }

        viewModelScope.launch {
            // 1. Crear la Purchase
            val purchase = Purchase(
                userId = userId.toInt(),
                purchaseDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                totalAmount = total,
                paidAmount = paid,
                changeAmount = paid - total
            )

            // 2. Crear los PurchaseItem a partir de CartItem
            val purchaseItems = _cartItems.value.map { cartItem ->
                PurchaseItem(
                    purchaseId = 0, // Se actualizará al insertar la Purchase
                    productId = cartItem.product.id.toInt(),
                    productName = cartItem.product.name,
                    productDescription = cartItem.product.description,
                    productPrice = cartItem.product.price,
                    quantity = cartItem.quantity
                )
            }

            // 3. Guardar la compra y sus ítems en la base de datos
            purchaseRepository.savePurchase(purchase, purchaseItems)

            // 4. Actualizar el stock de los productos en la base de datos
            _cartItems.value.forEach { cartItem ->
                val updatedStock = cartItem.product.stock - cartItem.quantity
                val updatedProduct = cartItem.product.copy(stock = updatedStock)
                pharmacyProductRepository.updateProduct(updatedProduct)
            }

            _paymentMessage.value = "Pago exitoso. Vuelto: S/. ${String.format(Locale.getDefault(), "%.2f", paid - total)}"
            clearCart() // Limpiar carrito después del pago exitoso
        }
        return true
    }

    fun clearPaymentMessage() {
        _paymentMessage.value = null
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _paidAmount.value = ""
        _changeAmount.value = 0.0
        _totalAmount.value = 0.0
    }
}