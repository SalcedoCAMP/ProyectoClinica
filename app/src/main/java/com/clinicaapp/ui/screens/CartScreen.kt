// com.clinicaapp.ui.screens.CartScreen.kt
package com.clinicaapp.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.clinicaapp.navigation.AppScreens
import com.clinicaapp.ui.viewmodel.CartItem
import com.clinicaapp.ui.viewmodel.CartViewModel
import com.clinicaapp.ui.viewmodel.SharedViewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.clinicaapp.data.db.AppDatabase
import com.clinicaapp.data.repository.PharmacyProductRepository // Importa el repositorio
import com.clinicaapp.data.repository.PurchaseRepository // Importa el repositorio
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel,

) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context) // INSTANCIA DE LA BD

    // Crear las instancias de los repositorios
    val purchaseRepository = remember { PurchaseRepository(db.purchaseDao()) }
    val pharmacyProductRepository = remember { PharmacyProductRepository(db.pharmacyProductDao()) }

    // Ahora, inicializa el CartViewModel con las instancias de los repositorios
    val cartViewModel: CartViewModel =
        viewModel(
            factory = CartViewModelFactory(
                purchaseRepository,
                pharmacyProductRepository
            )
        )

    val cartItems by cartViewModel.cartItems.collectAsState()
    val totalAmount by cartViewModel.totalAmount.collectAsState()
    val paidAmount by cartViewModel.paidAmount.collectAsState()
    val changeAmount by cartViewModel.changeAmount.collectAsState()
    val paymentMessage by cartViewModel.paymentMessage.collectAsState()
    val currentUser by sharedViewModel.currentUser.collectAsState()
    // val context = LocalContext.current // Ya se define arriba

    // Para escanear productos directamente en el carrito
    val pharmacyProductDao = db.pharmacyProductDao()


    val barcodeLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let { qrContent ->
            if (qrContent.startsWith("PRODUCT_ID:")) {
                val productId = qrContent.removePrefix("PRODUCT_ID:").toLongOrNull()
                if (productId != null) {
                    // Para buscar el producto y añadirlo al carrito, necesitamos ejecutar esto en un coroutine
                    // y usar el pharmacyProductRepository
                    CoroutineScope(Dispatchers.IO).launch {
                        val product = pharmacyProductRepository.getProductById(productId)
                        withContext(Dispatchers.Main) {
                            if (product != null) {
                                cartViewModel.addProductToCart(product)
                                Toast.makeText(context, "${product.name} agregado al carrito.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Producto no encontrado para el QR escaneado.", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, "QR de producto inválido", Toast.LENGTH_SHORT).show()
                }
            } else if (qrContent == "PAYMENT_QR_CLINICA") {
                val userId = currentUser?.id
                if (userId != null) {
                    if (cartViewModel.processPayment(userId)) {
                        Toast.makeText(context, paymentMessage, Toast.LENGTH_LONG).show()
                        generatePdfReceipt(context, currentUser?.name ?: "Usuario", cartItems, totalAmount, paidAmount.toDoubleOrNull() ?: 0.0, changeAmount)
                        navController.popBackStack(AppScreens.PharmacyProductsList, inclusive = false)
                    } else {
                        Toast.makeText(context, paymentMessage, Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(context, "Error: Usuario no logueado.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Contenido QR desconocido: $qrContent", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(paymentMessage) {
        if (paymentMessage != null && !paymentMessage!!.contains("stock")) {
            Toast.makeText(context, paymentMessage, Toast.LENGTH_LONG).show()
            cartViewModel.clearPaymentMessage()
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Carrito de Compras") },
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
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (cartItems.isEmpty()) {
                Text(
                    text = "Tu carrito está vacío.",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(32.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(cartItems) { item ->
                        CartItemCard(item = item, cartViewModel = cartViewModel)
                    }
                }

                Divider(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Total a Pagar: S/. ${String.format(Locale.getDefault(), "%.2f", totalAmount)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = paidAmount,
                        onValueChange = { cartViewModel.setPaidAmount(it) },
                        label = { Text("Monto con el que pagas") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )

                    Text(
                        text = "Vuelto: S/. ${String.format(Locale.getDefault(), "%.2f", changeAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { barcodeLauncher.launch(ScanOptions().apply { setPrompt("Escanea el QR de un producto") }) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            enabled = currentUser != null
                        ) {
                            Icon(Icons.Filled.QrCodeScanner, contentDescription = "Escanear QR de Producto", modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Escanear Producto")
                        }
                        Spacer(Modifier.width(16.dp))
                        Button(
                            onClick = {
                                if (currentUser != null) {
                                    val options = ScanOptions()
                                    options.setPrompt("Escanea el QR para pagar")
                                    barcodeLauncher.launch(options)
                                } else {
                                    Toast.makeText(context, "Debes iniciar sesión para pagar.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            enabled = currentUser != null && cartItems.isNotEmpty() && (paidAmount.toDoubleOrNull() ?: 0.0) >= totalAmount
                        ) {
                            Text("Pagar con QR")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun CartItemCard(item: CartItem, cartViewModel: CartViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Unitario: S/. ${String.format(Locale.getDefault(), "%.2f", item.product.price)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Total: S/. ${String.format(Locale.getDefault(), "%.2f", item.subtotal)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { cartViewModel.updateCartItemQuantity(item.product, item.quantity - 1) }) {
                    Icon(Icons.Filled.Remove, contentDescription = "Disminuir")
                }
                Text(text = item.quantity.toString(), modifier = Modifier.padding(horizontal = 8.dp))
                IconButton(onClick = { cartViewModel.updateCartItemQuantity(item.product, item.quantity + 1) }) {
                    Icon(Icons.Filled.Add, contentDescription = "Aumentar")
                }
                IconButton(onClick = { cartViewModel.removeProductFromCart(item.product) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}

// Función para generar el PDF (simplificado)
fun generatePdfReceipt(
    context: Context,
    userName: String,
    cartItems: List<CartItem>,
    totalAmount: Double,
    paidAmount: Double,
    changeAmount: Double
) {
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = document.startPage(pageInfo)
    val canvas = page.canvas
    val paint = android.graphics.Paint()

    var y = 50f
    val x = 50f
    val lineHeight = 20f

    paint.textSize = 24f
    canvas.drawText("Recibo de Compra - ClínicaApp Farmacia", x, y, paint)
    y += lineHeight * 2

    paint.textSize = 12f
    canvas.drawText("Usuario: $userName", x, y, paint)
    y += lineHeight
    canvas.drawText("Fecha: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}", x, y, paint)
    y += lineHeight * 2

    paint.textSize = 14f
    canvas.drawText("Productos:", x, y, paint)
    y += lineHeight

    paint.textSize = 12f
    cartItems.forEach { item ->
        canvas.drawText("${item.quantity} x ${item.product.name} @ S/. ${String.format(Locale.getDefault(), "%.2f", item.product.price)} = S/. ${String.format(Locale.getDefault(), "%.2f", item.subtotal)}", x + 10, y, paint)
        y += lineHeight
    }
    y += lineHeight

    paint.textSize = 16f
    canvas.drawText("Total Pagado: S/. ${String.format(Locale.getDefault(), "%.2f", totalAmount)}", x, y, paint)
    y += lineHeight
    canvas.drawText("Monto Entregado: S/. ${String.format(Locale.getDefault(), "%.2f", paidAmount)}", x, y, paint)
    y += lineHeight
    canvas.drawText("Vuelto: S/. ${String.format(Locale.getDefault(), "%.2f", changeAmount)}", x, y, paint)

    document.finishPage(page)

    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "recibo_clinica_farmacia_$timeStamp.pdf"
    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    val file = File(downloadsDir, fileName)

    try {
        FileOutputStream(file).use { fos ->
            document.writeTo(fos)
        }
        Toast.makeText(context, "Recibo guardado en Descargas: $fileName", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Error al guardar el recibo: ${e.message}", Toast.LENGTH_LONG).show()
        e.printStackTrace()
    } finally {
        document.close()
    }
}