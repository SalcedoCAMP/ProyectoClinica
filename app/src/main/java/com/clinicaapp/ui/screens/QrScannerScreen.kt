// com.clinicaapp.ui.screens.QrScannerScreen.kt
package com.clinicaapp.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.clinicaapp.data.db.AppDatabase
import com.clinicaapp.ui.viewmodel.CartViewModel
import com.clinicaapp.data.repository.PharmacyProductRepository
import com.clinicaapp.data.repository.PurchaseRepository
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    navController: NavController,
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)

    val purchaseRepository = remember { PurchaseRepository(db.purchaseDao()) }
    val pharmacyProductRepository = remember { PharmacyProductRepository(db.pharmacyProductDao()) }

    val cartViewModel: CartViewModel =
        viewModel(
            factory = CartViewModelFactory(
                purchaseRepository,
                pharmacyProductRepository
            )
        )

    // *** Mueve la declaración de barcodeLauncher AQUÍ, ANTES de requestPermissionLauncher ***
    val barcodeLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let { qrContent ->
            val productId = qrContent.toLongOrNull()
            if (productId != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    val product = pharmacyProductRepository.getProductById(productId)
                    withContext(Dispatchers.Main) {
                        if (product != null) {
                            cartViewModel.addProductToCart(product)
                            Toast.makeText(context, "${product.name} agregado al carrito.", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        } else {
                            Toast.makeText(context, "Producto no encontrado para el QR escaneado.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } else {
                Toast.makeText(context, "QR de producto inválido. Esperaba un ID numérico.", Toast.LENGTH_LONG).show()
            }
        } ?: run {
            Toast.makeText(context, "Escaneo cancelado.", Toast.LENGTH_SHORT).show()
        }
        navController.popBackStack()
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permiso concedido, lanzar el escáner
            barcodeLauncher.launch(ScanOptions().apply {
                setPrompt("Escanea el código QR de un producto")
            })
        } else {
            Toast.makeText(context, "Permiso de cámara denegado. No se puede escanear QR.", Toast.LENGTH_LONG).show()
            navController.popBackStack()
        }
    }

    LaunchedEffect(Unit) {
        if (context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            barcodeLauncher.launch(ScanOptions().apply {
                setPrompt("Escanea el código QR de un producto")
            })
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escanear QR") },
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
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Iniciando escáner QR...")
        }
    }
}