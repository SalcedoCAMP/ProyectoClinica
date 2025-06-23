package com.clinicaapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.clinicaapp.data.db.AppDatabase
import com.clinicaapp.data.db.entities.Doctor
import com.clinicaapp.data.repository.DoctorRepository
import com.clinicaapp.ui.viewmodel.DoctorManagementViewModel
import com.clinicaapp.ui.viewmodel.DoctorManagementViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDoctorScreen(navController: NavController, doctorId: Long? = null) {
    val context = LocalContext.current
    val doctorRepository = remember { DoctorRepository(AppDatabase.getDatabase(context).doctorDao()) }
    val doctorManagementViewModel: DoctorManagementViewModel = viewModel(
        factory = DoctorManagementViewModelFactory(doctorRepository)
    )

    var doctorName by remember { mutableStateOf("") }
    var doctorSpecialty by remember { mutableStateOf("") }
    var doctorSchedule by remember { mutableStateOf("") }

    // Si doctorId es -1L, se está añadiendo un nuevo doctor; de lo contrario, se edita.
    val isEditing = doctorId != -1L

    // Cargar el doctor si estamos editando
    LaunchedEffect(doctorId) {
        if (isEditing) {
            doctorManagementViewModel.loadDoctorById(doctorId!!)
        } else {
            doctorManagementViewModel.clearSelectedDoctor()
        }
    }

    val selectedDoctor by doctorManagementViewModel.selectedDoctor.collectAsState()

    LaunchedEffect(selectedDoctor) {
        selectedDoctor?.let {
            doctorName = it.name
            doctorSpecialty = it.specialty
            doctorSchedule = it.schedule
        } ?: run {
            // Si selectedDoctor es null (por ejemplo, al borrar un doctor editado o al añadir uno nuevo)

            if (!isEditing) {
                doctorName = ""
                doctorSpecialty = ""
                doctorSchedule = ""
            }
        }
    }

    val actionMessage by doctorManagementViewModel.actionMessage.collectAsState()

    LaunchedEffect(actionMessage) {
        actionMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            doctorManagementViewModel.clearActionMessage()
            if (it.contains("exitosamente")) {
                navController.popBackStack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Doctor" else "Agregar Nuevo Doctor") },
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
                text = if (isEditing) "Actualizar Información del Doctor" else "Ingresar Datos del Nuevo Doctor",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = doctorName,
                onValueChange = { doctorName = it },
                label = { Text("Nombre del Doctor") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Nombre") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape = RoundedCornerShape(8.dp)
            )

            OutlinedTextField(
                value = doctorSpecialty,
                onValueChange = { doctorSpecialty = it },
                label = { Text("Especialidad") },
                leadingIcon = { Icon(Icons.Default.MedicalServices, contentDescription = "Especialidad") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape = RoundedCornerShape(8.dp)
            )

            OutlinedTextField(
                value = doctorSchedule,
                onValueChange = { doctorSchedule = it },
                label = { Text("Horario (Ej: L-V 9:00-17:00)") },
                leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = "Horario") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (doctorName.isNotBlank() && doctorSpecialty.isNotBlank() && doctorSchedule.isNotBlank()) {
                        val doctor = Doctor(
                            id = if (isEditing) doctorId!! else 0L,
                            name = doctorName,
                            specialty = doctorSpecialty,
                            schedule = doctorSchedule
                        )
                        if (isEditing) {
                            doctorManagementViewModel.updateDoctor(doctor)
                        } else {
                            doctorManagementViewModel.addDoctor(doctor)
                        }
                    } else {
                        Toast.makeText(context, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isEditing) "Actualizar Doctor" else "Guardar Doctor")
            }
        }
    }
}