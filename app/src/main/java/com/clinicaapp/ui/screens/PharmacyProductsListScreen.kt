package com.clinicaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.clinicaapp.data.db.entities.PharmacyProduct
import com.clinicaapp.navigation.AppScreens
import com.clinicaapp.ui.viewmodel.CartViewModel
import com.clinicaapp.ui.viewmodel.PharmacyProductViewModel
import com.clinicaapp.ui.viewmodel.SharedViewModel
import com.clinicaapp.data.repository.PharmacyProductRepository
import com.clinicaapp.data.repository.PurchaseRepository
import com.clinicaapp.data.db.AppDatabase
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.ViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PharmacyProductsListScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel,
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)

    val pharmacyProductRepository = remember { PharmacyProductRepository(db.pharmacyProductDao()) }
    val purchaseRepository = remember { PurchaseRepository(db.purchaseDao()) }

    val pharmacyProductViewModel: PharmacyProductViewModel =
        viewModel(
            factory = PharmacyProductViewModelFactory(pharmacyProductRepository)
        )
    val cartViewModel: CartViewModel =
        viewModel(
            factory = CartViewModelFactory(purchaseRepository, pharmacyProductRepository)
        )

    val products by pharmacyProductViewModel.allProducts.collectAsState()
    val searchQuery by pharmacyProductViewModel.searchQuery.collectAsState()
    val cartItems by cartViewModel.cartItems.collectAsState()
    val paymentMessage by cartViewModel.paymentMessage.collectAsState()

    LaunchedEffect(paymentMessage) {
        if (paymentMessage != null) {
            println("Mensaje del carrito: $paymentMessage")
            if (paymentMessage!!.contains("stock")) {
                // Si es un mensaje de stock, lo dejamos visible un momento
            } else {
                cartViewModel.clearPaymentMessage()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Farmacia") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(AppScreens.QrScanner) }) {
                        Icon(Icons.Filled.QrCodeScanner, contentDescription = "Escanear QR de producto")
                    }
                    IconButton(onClick = { navController.navigate(AppScreens.Cart) }) {
                        BadgedBox(badge = {
                            if (cartItems.isNotEmpty()) {
                                Badge { Text(cartItems.size.toString()) }
                            }
                        }) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = "Carrito de Compras")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { pharmacyProductViewModel.setSearchQuery(it) },
                label = { Text("Buscar productos...") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            if (products.isEmpty() && searchQuery.isNotBlank()) {
                Text(
                    text = "No se encontraron productos para \"$searchQuery\".",
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
                )
            } else if (products.isEmpty()) {
                Text(
                    text = "No hay productos de farmacia disponibles.",
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(products) { product ->
                        ProductCard(product = product, cartViewModel = cartViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(product: PharmacyProduct, cartViewModel: CartViewModel) {
    var quantityToAdd by remember { mutableStateOf(1) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column( // Usamos Column para apilar la información y luego los controles
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row( // Esta Row contiene la imagen y la información del producto
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Imagen del producto
                if (!product.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = "Imagen de ${product.name}",
                        modifier = Modifier
                            .size(80.dp)
                            .padding(end = 16.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.LocalGroceryStore,
                        contentDescription = "No hay imagen",
                        modifier = Modifier
                            .size(80.dp)
                            .padding(end = 16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = product.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Precio: S/. ${String.format(Locale.getDefault(), "%.2f", product.price)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Stock: ${product.stock} unidades",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (product.stock == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp)) // Espacio entre la info del producto y los controles

            // Nueva Row para los controles de cantidad y el botón de añadir al carrito
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween // Distribuye el espacio entre los elementos
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { if (quantityToAdd > 1) quantityToAdd-- },
                        enabled = quantityToAdd > 1
                    ) {
                        Icon(Icons.Filled.Remove, contentDescription = "Disminuir cantidad")
                    }
                    OutlinedTextField(
                        value = quantityToAdd.toString(),
                        onValueChange = {
                            quantityToAdd = it.toIntOrNull() ?: 1
                            if (quantityToAdd < 1) quantityToAdd = 1
                        },
                        modifier = Modifier.width(60.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        label = { Text("Cant.") },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    )
                    IconButton(
                        onClick = { if (quantityToAdd < product.stock) quantityToAdd++ },
                        enabled = quantityToAdd < product.stock
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Aumentar cantidad")
                    }
                }

                // Botón "Agregar al Carrito"
                Button(
                    onClick = { cartViewModel.addProductToCart(product, quantityToAdd) },
                    enabled = product.stock > 0 && quantityToAdd > 0,
                    modifier = Modifier.weight(1f) // Esto hará que el botón ocupe el espacio restante
                        .padding(start = 8.dp) // Pequeño padding a la izquierda del botón
                ) {
                    Text("Agregar al Carrito")
                }
            }
        }
    }
}

// Factories para ViewModels (mantener en un archivo separado idealmente)
class PharmacyProductViewModelFactory(private val repository: PharmacyProductRepository) :
    androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PharmacyProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PharmacyProductViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class CartViewModelFactory(
    private val purchaseRepository: PurchaseRepository,
    private val pharmacyProductRepository: PharmacyProductRepository
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CartViewModel(purchaseRepository, pharmacyProductRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}