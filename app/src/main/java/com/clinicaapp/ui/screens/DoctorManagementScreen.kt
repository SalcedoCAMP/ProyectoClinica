package com.clinicaapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
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
import com.clinicaapp.data.db.entities.Doctor
import com.clinicaapp.data.repository.DoctorRepository
import com.clinicaapp.navigation.AppScreens // Asegúrate de que esta importación es correcta
import com.clinicaapp.ui.viewmodel.DoctorManagementViewModel
import com.clinicaapp.ui.viewmodel.DoctorManagementViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorManagementScreen(navController: NavController) {
    val context = LocalContext.current
    val doctorRepository = remember { DoctorRepository(AppDatabase.getDatabase(context).doctorDao()) }
    val doctorManagementViewModel: DoctorManagementViewModel = viewModel(
        factory = DoctorManagementViewModelFactory(doctorRepository)
    )

    val doctors by doctorManagementViewModel.doctors.collectAsState()
    val actionMessage by doctorManagementViewModel.actionMessage.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var doctorToDelete by remember { mutableStateOf<Doctor?>(null) }

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            doctorManagementViewModel.clearActionMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Doctores") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    // CORRECTO: Llama a addEditDoctorRoute sin argumento, que usará -1L por defecto
                    IconButton(onClick = { navController.navigate(AppScreens.addEditDoctorRoute()) }) { // <-- CAMBIO AQUÍ
                        Icon(Icons.Filled.Add, contentDescription = "Agregar Doctor")
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
                text = "Doctores Registrados",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (doctors.isEmpty()) {
                Text("No hay doctores registrados.", style = MaterialTheme.typography.bodyLarge)
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(doctors) { doctor ->
                        DoctorManagementCard(
                            doctor = doctor,
                            onEditClick = {
                                // CORRECTO: Llama a addEditDoctorRoute con el ID
                                navController.navigate(AppScreens.addEditDoctorRoute(doctor.id))
                            },
                            onDeleteClick = {
                                doctorToDelete = doctor
                                showDeleteDialog = true
                            },
                            onViewAppointmentsClick = {
                                // CORRECTO: Llama a adminAppointmentsForDoctorRoute con el ID
                                navController.navigate(AppScreens.adminAppointmentsForDoctorRoute(doctor.id))
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; doctorToDelete = null },
            title = { Text("Confirmar Eliminación") },
            text = { Text("¿Estás seguro de que quieres eliminar al doctor ${doctorToDelete?.name}?") },
            confirmButton = {
                Button(onClick = {
                    doctorToDelete?.let { doctorManagementViewModel.deleteDoctor(it) }
                    showDeleteDialog = false
                    doctorToDelete = null
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; doctorToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorManagementCard(
    doctor: Doctor,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onViewAppointmentsClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = doctor.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Especialidad: ${doctor.specialty}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Horario: ${doctor.schedule}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onViewAppointmentsClick) {
                    Icon(Icons.Default.Info, contentDescription = "Ver Citas")
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}