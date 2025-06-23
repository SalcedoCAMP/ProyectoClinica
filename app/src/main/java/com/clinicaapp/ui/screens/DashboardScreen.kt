// com.clinicaapp.ui.screens.DashboardScreen.kt (MODIFICADO)
package com.clinicaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.clinicaapp.navigation.AppScreens
import com.clinicaapp.ui.viewmodel.SharedViewModel
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Info
import kotlinx.coroutines.launch
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.LocalPharmacy // NUEVO ICONO para farmacia

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, sharedViewModel: SharedViewModel) {
    val currentUser by sharedViewModel.currentUser.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(280.dp)
            ) {
                // Encabezado del Drawer
                DrawerHeaderUser(currentUser?.name ?: "Usuario")
                Spacer(Modifier.height(16.dp))

                // Item "Nosotros"
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.Info, contentDescription = null) },
                    label = { Text("Nosotros") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                Spacer(Modifier.weight(1f))

                // BOTON CERRAR SESIÓN
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                    label = { Text("Cerrar Sesión") },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        sharedViewModel.clearCurrentUser()
                        navController.navigate(AppScreens.Login) {
                            popUpTo(AppScreens.Login) { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Hola, ${currentUser?.name ?: "Usuario"}", style = MaterialTheme.typography.titleLarge)
                            val displayRole = when (currentUser?.role) {
                                "user" -> "Usuario"
                                "admin" -> "Administrador"
                                else -> "Desconocido"
                            }
                            Text("Rol: $displayRole", style = MaterialTheme.typography.bodySmall)
                        }
                    },
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
                            navController.navigate(AppScreens.Profile)
                        }) {
                            Icon(Icons.Filled.Person, contentDescription = "Mi Perfil")
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
                verticalArrangement = Arrangement.spacedBy(16.dp)

            ) {
                Spacer(modifier = Modifier.height(24.dp))
                DashboardCard(
                    icon = Icons.Filled.MedicalServices,
                    title = "Ver Médicos",
                    description = "Explora la lista de médicos disponibles",
                    onClick = { navController.navigate(AppScreens.DoctorsList) }
                )

                DashboardCard(
                    icon = Icons.Filled.CalendarMonth,
                    title = "Reservar Cita",
                    description = "Agenda tu próxima cita médica.",
                    onClick = { navController.navigate(AppScreens.bookAppointmentRoute()) }
                )

                DashboardCard(
                    icon = Icons.Filled.List,
                    title = "Mis Citas",
                    description = "Revisa y gestiona tus citas reservadas.",
                    onClick = { navController.navigate(AppScreens.Appointments) }
                )

                // NUEVA TARJETA PARA FARMACIA (ROL USUARIO)
                DashboardCard(
                    icon = Icons.Filled.LocalPharmacy,
                    title = "Farmacia",
                    description = "Explora y compra productos de farmacia.",
                    onClick = { navController.navigate(AppScreens.PharmacyProductsList) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = MaterialTheme.shapes.medium,
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DrawerHeaderUser(userName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hola, $userName",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(8.dp))
        Divider()
    }
}