// com.clinicaapp.ui.screens.PurchaseVouchersScreen.kt
package com.clinicaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.clinicaapp.data.db.AppDatabase
import com.clinicaapp.data.repository.PurchaseRepository
import com.clinicaapp.ui.viewmodel.PurchaseViewModel
import com.clinicaapp.ui.viewmodel.PurchaseViewModelFactory
import com.clinicaapp.data.db.entities.PurchaseWithItems
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseVouchersScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val purchaseRepository = remember { PurchaseRepository(db.purchaseDao()) }
    val purchaseViewModel: PurchaseViewModel = viewModel(
        factory = PurchaseViewModelFactory(purchaseRepository)
    )

    val purchases by purchaseViewModel.allPurchasesWithItems.collectAsState()
    // val searchQuery by purchaseViewModel.searchQuery.collectAsState() // Si implementas búsqueda

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Compras") },
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
                .padding(horizontal = 16.dp)
        ) {


            if (purchases.isEmpty()) {
                Text(
                    text = "No hay compras registradas.",
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(32.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(purchases) { purchaseWithItems ->
                        PurchaseVoucherCard(purchaseWithItems = purchaseWithItems)
                    }
                }
            }
        }
    }
}

@Composable
fun PurchaseVoucherCard(purchaseWithItems: PurchaseWithItems) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            Text(
                text = "Compra #${purchaseWithItems.purchase.id} - ${dateFormat.format(purchaseWithItems.purchase.purchaseDate)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Usuario ID: ${purchaseWithItems.purchase.userId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Detalles de los ítems comprados
            Text(
                text = "Productos:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            purchaseWithItems.items.forEach { item ->
                Text(
                    text = "  - ${item.quantity} x ${item.productName} (S/. ${String.format(Locale.getDefault(), "%.2f", item.productPrice)} c/u)",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total: S/. ${String.format(Locale.getDefault(), "%.2f", purchaseWithItems.purchase.totalAmount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Pagado: S/. ${String.format(Locale.getDefault(), "%.2f", purchaseWithItems.purchase.paidAmount)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Vuelto: S/. ${String.format(Locale.getDefault(), "%.2f", purchaseWithItems.purchase.changeAmount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}