// com.clinicaapp.ui.screens.AddEditPharmacyProductScreen.kt
package com.clinicaapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.clinicaapp.data.db.AppDatabase
import com.clinicaapp.data.db.entities.PharmacyProduct
import com.clinicaapp.data.repository.PharmacyProductRepository
import com.clinicaapp.ui.viewmodel.PharmacyProductViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch // Necesario para el CoroutineScope
import kotlinx.coroutines.Dispatchers // Necesario para los Dispatchers
import kotlinx.coroutines.withContext // Necesario para cambiar de contexto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPharmacyProductScreen(
    navController: NavController,
    productId: Long? = null // Nullable para indicar si es añadir o editar
) {
    val context = LocalContext.current
    val pharmacyProductRepository = remember { PharmacyProductRepository(AppDatabase.getDatabase(context).pharmacyProductDao()) }
    val pharmacyProductViewModel: PharmacyProductViewModel = viewModel(
        factory = PharmacyProductViewModelFactory(pharmacyProductRepository)
    )

    // Estados para el formulario
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    val isEditing = productId != null && productId != -1L

    // Efecto para cargar los datos del producto si estamos en modo edición
    LaunchedEffect(isEditing, productId) {
        if (isEditing && productId != null) {
            // Cargar el producto existente
            val productToEdit = pharmacyProductRepository.getProductById(productId)
            productToEdit?.let {
                name = it.name
                description = it.description
                price = it.price.toString()
                stock = it.stock.toString()
                imageUrl = it.imageUrl ?: ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Producto" else "Añadir Producto") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre del Producto") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = price,
                onValueChange = { newValue ->
                    // Solo permite números y un punto decimal
                    val filteredValue = newValue.filter { it.isDigit() || it == '.' }
                    if (filteredValue.count { it == '.' } <= 1) { // Asegura solo un punto decimal
                        price = filteredValue
                    }
                },
                label = { Text("Precio (S/.)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = stock,
                onValueChange = { newValue ->
                    // Solo permite números enteros
                    val filteredValue = newValue.filter { it.isDigit() }
                    stock = filteredValue
                },
                label = { Text("Stock") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("URL de la Imagen (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val parsedPrice = price.toDoubleOrNull()
                    val parsedStock = stock.toIntOrNull()

                    if (name.isBlank() || parsedPrice == null || parsedStock == null) {
                        Toast.makeText(context, "Por favor, completa todos los campos requeridos y asegúrate de que el precio y el stock sean válidos.", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    // Lanzamos una coroutine para la operación de base de datos
                    CoroutineScope(Dispatchers.IO).launch {
                        val product = PharmacyProduct(
                            id = if (isEditing) productId!! else 0, // Si es edición, usa el ID existente, si no, Room generará uno (0)
                            name = name,
                            description = description,
                            price = parsedPrice,
                            stock = parsedStock,
                            imageUrl = imageUrl.ifBlank { null } // Guarda null si la URL está vacía
                        )

                        if (isEditing) {
                            pharmacyProductViewModel.updateProduct(product)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Producto actualizado exitosamente", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            pharmacyProductViewModel.insertProduct(product)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Producto añadido exitosamente", Toast.LENGTH_SHORT).show()
                            }
                        }
                        withContext(Dispatchers.Main) {
                            navController.popBackStack() // Regresar a la lista de administración
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(Icons.Filled.Save, contentDescription = "Guardar")
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isEditing) "Guardar Cambios" else "Añadir Producto")
            }
        }
    }
}