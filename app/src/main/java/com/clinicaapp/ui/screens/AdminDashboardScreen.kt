// com.clinicaapp.ui.screens.AdminDashboardScreen.kt (CORREGIDO)
package com.clinicaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocalGroceryStore // Icono para gestión de productos
import androidx.compose.material.icons.filled.Receipt // Icono para historial de compras
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.clinicaapp.navigation.AppScreens // Asegúrate de que esta es tu ruta correcta a AppScreens
import com.clinicaapp.ui.viewmodel.SharedViewModel
import kotlinx.coroutines.launch

// Importa los iconos que necesites (los que ya tenías y son correctos)
// Si ReceiptLong te sigue dando una advertencia de deprecado, usa solo Receipt
// Aunque en este código lo voy a dejar porque lo tenías.
import androidx.compose.material.icons.filled.ReceiptLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navController: NavController, sharedViewModel: SharedViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .width(IntrinsicSize.Max)
            ) {
                DrawerHeaderAdmin()
                Spacer(Modifier.height(16.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Info, contentDescription = null) },
                    label = { Text("Nosotros") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Panel de Administrador") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.apply { if (isClosed) open() else close() }
                            }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Abrir menú lateral")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            sharedViewModel.setCurrentUser(null)
                            navController.navigate(AppScreens.Login) {
                                popUpTo(AppScreens.Login) { inclusive = true }
                            }
                        }) {
                            Icon(Icons.Filled.ExitToApp, contentDescription = "Cerrar Sesión")
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Bienvenido, Administrador",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 32.dp, top = 16.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AdminDashboardCard(
                        icon = Icons.Filled.PeopleAlt,
                        title = "Gestionar Doctores",
                        description = "Añade, edita o elimina registros de doctores."
                    ) {
                        navController.navigate(AppScreens.DoctorManagement)
                    }

                    AdminDashboardCard(
                        icon = Icons.Filled.DateRange,
                        title = "Ver Todas las Citas",
                        description = "Revisa y gestiona las citas de todos los usuarios."
                    ) {
                        navController.navigate(AppScreens.AdminAppointmentsListBase) // Asegúrate que esta es la ruta correcta
                    }

                    // ÚNICA TARJETA PARA GESTIÓN DE PRODUCTOS DE FARMACIA
                    AdminDashboardCard(
                        icon = Icons.Filled.LocalGroceryStore, // Icono para farmacia
                        title = "Gestionar Productos Farmacia",
                        description = "Añade, edita o elimina productos de la farmacia."
                    ) {
                        navController.navigate(AppScreens.AdminPharmacyProducts) // Ruta corregida
                    }

                    // ÚNICA TARJETA PARA HISTORIAL DE COMPRAS (VOUCHERS)
                    AdminDashboardCard(
                        icon = Icons.Filled.ReceiptLong, // Puedes usar Receipt o ReceiptLong
                        title = "Historial de Compras",
                        description = "Revisa el historial de ventas de farmacia."
                    ) {
                        navController.navigate(AppScreens.PurchaseVouchers)
                    }

                }
            }
        }
    }
}

// Los @Composable AdminDashboardCard y DrawerHeaderAdmin se mantienen igual
@Composable
fun AdminDashboardCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DrawerHeaderAdmin() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Menú de Administrador",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Divider()
    }
}