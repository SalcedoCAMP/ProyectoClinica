package com.clinicaapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.clinicaapp.data.db.AppDatabase
import com.clinicaapp.data.repository.AppointmentRepository
import com.clinicaapp.data.repository.DoctorRepository
import com.clinicaapp.ui.viewmodel.AppointmentViewModel
import com.clinicaapp.ui.viewmodel.AppointmentViewModelFactory
import com.clinicaapp.ui.viewmodel.AppointmentWithDoctor
import com.clinicaapp.ui.viewmodel.SharedViewModel

/**
  Muestra las citas del usuario y permite cancelarlas
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(navController: NavController, sharedViewModel: SharedViewModel) {
    val context = LocalContext.current
    val appointmentRepository = remember { AppointmentRepository(AppDatabase.getDatabase(context).appointmentDao()) }
    val doctorRepository = remember { DoctorRepository(AppDatabase.getDatabase(context).doctorDao()) }
    val appointmentViewModel: AppointmentViewModel = viewModel(
        factory = AppointmentViewModelFactory(appointmentRepository, doctorRepository)
    )

    val currentUser by sharedViewModel.currentUser.collectAsState()
    val userId = currentUser?.id ?: -1L

    val userAppointments by appointmentViewModel.userAppointments.collectAsState()
    val appointmentMessage by appointmentViewModel.appointmentMessage.collectAsState()

    // Controla si se muestra el diálogo de confirmación
    var showCancelDialog by remember { mutableStateOf(false) }
    // Estado para guardar la cita que se intenta cancelar
    var appointmentToCancel by remember { mutableStateOf<AppointmentWithDoctor?>(null) }


    // Cargar citas cuando el userId esté disponible
    LaunchedEffect(userId) {
        if (userId != -1L) {
            appointmentViewModel.loadUserAppointments(userId)
        }
    }

    // Observar mensajes de cancelación de cita
    LaunchedEffect(appointmentMessage) {
        appointmentMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            appointmentViewModel.clearMessage()
            if (userId != -1L) {
                appointmentViewModel.loadUserAppointments(userId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Citas") },
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
                text = "Tus Citas Reservadas",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (userAppointments.isEmpty()) {
                Text(
                    text = "No tienes citas reservadas aún.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(userAppointments) { appointmentWithDoctor ->
                        AppointmentCard(appointmentWithDoctor = appointmentWithDoctor) {
                            // Cuando se hace clic en "Cancelar Cita" en la tarjeta
                            appointmentToCancel = appointmentWithDoctor // Guarda la cita a cancelar
                            showCancelDialog = true // Muestra el diálogo
                        }
                    }
                }
            }
        }
    }
    // DIALOGO DE CONFIRMACIÓN
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = {
                // Al hacer clic fuera o presionar atrás
                showCancelDialog = false
                appointmentToCancel = null // Limpiar la cita a cancelar
            },
            title = { Text("Confirmar Cancelación") },
            text = {
                Text("¿Estás seguro de que quieres cancelar la cita con el ${appointmentToCancel?.doctor?.name}" +
                        " el ${appointmentToCancel?.appointment?.date} a las ${appointmentToCancel?.appointment?.time}?") },
            confirmButton = {
                Button(onClick = {
                    appointmentToCancel?.let {
                        appointmentViewModel.cancelAppointment(it.appointment.id) // Ejecuta la cancelación
                    }
                    showCancelDialog = false // Oculta el diálogo
                    appointmentToCancel = null
                }) {
                    Text("Sí, Cancelar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCancelDialog = false
                    appointmentToCancel = null
                }) {
                    Text("No, mantener")
                }
            }
        )
    }
}

/**
 *Composable que muestra una tarjeta individual para cada cita.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentCard(appointmentWithDoctor: AppointmentWithDoctor, onCancelClick: () -> Unit) {
    val appointment = appointmentWithDoctor.appointment
    val doctor = appointmentWithDoctor.doctor

    val cardColor = if (appointment.isCancelled) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (appointment.isCancelled) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Cita con ${doctor.name}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (appointment.isCancelled) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Especialidad: ${doctor.specialty}",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
            Text(
                text = "Fecha: ${appointment.date}",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
            Text(
                text = "Hora: ${appointment.time}",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (appointment.isCancelled) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Cancel,
                        contentDescription = "Cita Cancelada",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cita Cancelada",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = onCancelClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = "Cancelar Cita", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancelar Cita")
                }
            }
        }
    }
}

