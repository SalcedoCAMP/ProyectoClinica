// com.clinicaapp.ui.screens.AdminPharmacyProductsScreen.kt (CORREGIDO)
package com.clinicaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.clinicaapp.data.db.AppDatabase
import com.clinicaapp.data.db.entities.PharmacyProduct
import com.clinicaapp.data.repository.PharmacyProductRepository
import com.clinicaapp.navigation.AppScreens // Asegúrate de que esta importación sea correcta
import com.clinicaapp.ui.viewmodel.PharmacyProductViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPharmacyProductsScreen(
    navController: NavController,
) {
    val context = LocalContext.current
    val pharmacyProductRepository = remember { PharmacyProductRepository(AppDatabase.getDatabase(context).pharmacyProductDao()) }
    val pharmacyProductViewModel: PharmacyProductViewModel = viewModel(
        factory = PharmacyProductViewModelFactory(pharmacyProductRepository)
    )

    val products by pharmacyProductViewModel.allProducts.collectAsState()
    val searchQuery by pharmacyProductViewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Administrar Productos de Farmacia") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // C O R R E C C I Ó N: Usa la función para navegar sin ID (para añadir)
                        navController.navigate(AppScreens.addEditPharmacyProductRoute())
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "Añadir nuevo producto")
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
                    text = "No hay productos de farmacia disponibles para administrar.",
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(products) { product ->
                        AdminProductCard(
                            product = product,
                            onEditClick = {
                                // C O R R E C C I Ó N: Usa la función para navegar con ID (para editar)
                                navController.navigate(AppScreens.addEditPharmacyProductRoute(product.id))
                            },
                            onDeleteClick = { pharmacyProductViewModel.deleteProduct(product) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminProductCard(
    product: PharmacyProduct,
    onEditClick: (PharmacyProduct) -> Unit,
    onDeleteClick: (PharmacyProduct) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del producto (similar a PharmacyProductsListScreen)
            if (!product.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = "Imagen de ${product.name}",
                    modifier = Modifier
                        .size(60.dp)
                        .padding(end = 16.dp),
                    alignment = Alignment.Center,
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                // Placeholder si no hay imagen
                Surface(
                    modifier = Modifier
                        .size(60.dp)
                        .padding(end = 16.dp),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = product.name.firstOrNull()?.uppercaseChar().toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.wrapContentSize(Alignment.Center)
                    )
                }
            }


            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Precio: S/. ${String.format(Locale.getDefault(), "%.2f", product.price)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Stock: ${product.stock}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (product.stock == 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onEditClick(product) }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar producto")
                }
                IconButton(onClick = { onDeleteClick(product) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar producto", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}