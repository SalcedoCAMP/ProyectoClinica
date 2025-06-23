package com.clinicaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.clinicaapp.data.db.AppDatabase
import com.clinicaapp.data.db.entities.Doctor
import com.clinicaapp.data.repository.DoctorRepository
import com.clinicaapp.navigation.AppScreens
import com.clinicaapp.ui.viewmodel.DoctorViewModel
import com.clinicaapp.ui.viewmodel.DoctorViewModelFactory

/**
  Lista de médicos filtrable por especialidad. Permite seleccionar uno para reservar cita.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorListScreen(navController: NavController) {
    val context = LocalContext.current
    val doctorRepository = remember { DoctorRepository(AppDatabase.getDatabase(context).doctorDao()) }

    val doctorViewModel: DoctorViewModel = viewModel(factory = DoctorViewModelFactory(doctorRepository))

    val doctors by doctorViewModel.doctors.collectAsState()
    val specialties by doctorViewModel.specialties.collectAsState()
    val selectedSpecialty by doctorViewModel.selectedSpecialty.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Médicos Disponibles") },
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
                .padding(16.dp)
        ) {
            // FILTRAR POR ESPECIALIDAD
            SpecialtyFilterDropdown(
                specialties = specialties,
                selectedSpecialty = selectedSpecialty,
                onSpecialtySelected = { doctorViewModel.filterDoctorsBySpecialty(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (doctors.isEmpty()) {
                Text(
                    text = "No se encontraron médicos para la especialidad seleccionada o no hay médicos registrados.",
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(doctors) { doctor ->
                        DoctorCard(doctor = doctor) {
                            navController.navigate(AppScreens.bookAppointmentRoute(doctor.id))
                        }
                    }
                }
            }
        }
    }
}

/**
 Tarjeta con info del médico y opción para reservar cita.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorCard(doctor: Doctor, onBookAppointmentClick: (Doctor) -> Unit) {
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
            Text(
                text = "Especialidad: ${doctor.specialty}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Horario: ${doctor.schedule}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { onBookAppointmentClick(doctor) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = "Reservar Cita", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reservar Cita")
            }
        }
    }
}

/**
Dropdown para filtrar por especialidad.
@param specialties Lista disponible
@param selectedSpecialty Selección actual
@param onSpecialtySelected Acción al seleccionar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialtyFilterDropdown(
    specialties: List<String>,
    selectedSpecialty: String?,
    onSpecialtySelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        // Añade "Todas" como opción inicial si no hay especialidad seleccionada
        val displayText = selectedSpecialty ?: "Todas"
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Filtrar por Especialidad") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // Opción para "Todas"
            DropdownMenuItem(
                text = { Text("Todas") },
                onClick = {
                    onSpecialtySelected(null) // Pasar null para "Todas"
                    expanded = false
                }
            )
            specialties.forEach { specialty ->
                DropdownMenuItem(
                    text = { Text(specialty) },
                    onClick = {
                        onSpecialtySelected(specialty)
                        expanded = false
                    }
                )
            }
        }
    }
}