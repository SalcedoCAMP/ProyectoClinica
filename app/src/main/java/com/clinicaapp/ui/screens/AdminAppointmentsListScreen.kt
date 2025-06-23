package com.clinicaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.clinicaapp.data.db.AppDatabase
import com.clinicaapp.data.repository.AppointmentRepository
import com.clinicaapp.data.repository.DoctorRepository
import com.clinicaapp.ui.viewmodel.AppointmentViewModel
import com.clinicaapp.ui.viewmodel.AppointmentViewModelFactory
import com.clinicaapp.ui.viewmodel.AppointmentWithDoctor // Asegúrate de importar la clase correcta
import com.clinicaapp.ui.components.AppointmentCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAppointmentsListScreen(navController: NavController, doctorId: Long? = null) {
    val context = LocalContext.current
    val appointmentRepository = remember { AppointmentRepository(AppDatabase.getDatabase(context).appointmentDao()) }
    val doctorRepository = remember { DoctorRepository(AppDatabase.getDatabase(context).doctorDao()) }
    val appointmentViewModel: AppointmentViewModel = viewModel(
        factory = AppointmentViewModelFactory(appointmentRepository, doctorRepository)
    )

    val appointments by appointmentViewModel.allAppointments.collectAsState()
    val message by appointmentViewModel.appointmentMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope() // Necesario para lanzar coroutines desde el onClick del diálogo

    // --- ESTADO PARA EL DIÁLOGO DE CONFIRMACIÓN ---
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var appointmentIdToDelete by remember { mutableStateOf<Long?>(null) } // Guarda el ID de la cita a eliminar

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            appointmentViewModel.clearMessage()
        }
    }

    LaunchedEffect(doctorId) {
        if (doctorId != null && doctorId != -1L) {
            appointmentViewModel.loadAppointmentsForDoctor(doctorId)
        } else {
            appointmentViewModel.loadAllAppointments()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (doctorId != null && doctorId != -1L) "Citas del Doctor" else "Todas las Citas") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
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
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = if (doctorId != null && doctorId != -1L) "Citas para el Doctor" else "Listado de Todas las Citas",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (appointments.isEmpty()) {
                Text("No hay citas para ${if (doctorId != null && doctorId != -1L) "este doctor" else "mostrar"}.", style = MaterialTheme.typography.bodyLarge)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(appointments) { appointmentWithDoctor ->
                        AppointmentCard(
                            appointmentWithDoctor = appointmentWithDoctor,
                            onDeleteClick = { id ->
                                appointmentIdToDelete = id // Guarda el ID
                                showDeleteConfirmationDialog = true // Muestra el diálogo
                            },
                            isAdminView = true
                        ) {
                            // "ver detalles" implementar
                        }
                    }
                }
            }
        }
    }

    // --- DIÁLOGO DE CONFIRMACIÓN DE ELIMINACIÓN ---
    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = {
                // Cerrar el diálogo si se toca fuera o se presiona atrás
                showDeleteConfirmationDialog = false
                appointmentIdToDelete = null
            },
            title = { Text("Confirmar Eliminación") },
            text = { Text("¿Estás seguro de que deseas eliminar esta cita?") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch { // eliminación en una corrutina
                            appointmentIdToDelete?.let { id ->
                                appointmentViewModel.deleteAppointmentById(id)
                                showDeleteConfirmationDialog = false // Ocultar el diálogo
                                appointmentIdToDelete = null // Limpiar el ID
                            }
                        }
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteConfirmationDialog = false
                        appointmentIdToDelete = null // Limpiar el ID
                    }
                ) {
                    Text("No")
                }
            }
        )
    }
}