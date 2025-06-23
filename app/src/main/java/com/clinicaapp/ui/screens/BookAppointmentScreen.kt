package com.clinicaapp.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Log
import android.widget.DatePicker
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.clinicaapp.data.db.AppDatabase
import com.clinicaapp.data.repository.AppointmentRepository
import com.clinicaapp.data.repository.DoctorRepository
import com.clinicaapp.navigation.AppScreens
import com.clinicaapp.ui.viewmodel.AppointmentViewModel
import com.clinicaapp.ui.viewmodel.AppointmentViewModelFactory
import com.clinicaapp.ui.viewmodel.SharedViewModel
import com.clinicaapp.data.db.entities.Appointment
import kotlinx.coroutines.launch
import java.util.Calendar
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Función helper para normalizar una fecha (quitarle la hora, minutos, segundos y milisegundos)
fun Date.normalized(): Date {
    val cal = Calendar.getInstance()
    cal.time = this
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.time
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAppointmentScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel,
    doctorId: Long? = null
) {
    val context = LocalContext.current
    val appointmentRepository = remember { AppointmentRepository(AppDatabase.getDatabase(context).appointmentDao()) }
    val doctorRepository = remember { DoctorRepository(AppDatabase.getDatabase(context).doctorDao()) }
    val appointmentViewModel: AppointmentViewModel = viewModel(
        factory = AppointmentViewModelFactory(appointmentRepository, doctorRepository)
    )

    val currentUser by sharedViewModel.currentUser.collectAsState()
    val userId = currentUser?.id ?: -1L

    var selectedDoctor by remember { mutableStateOf<com.clinicaapp.data.db.entities.Doctor?>(null) }
    var selectedDateString by remember { mutableStateOf("") }
    var selectedTimeString by remember { mutableStateOf("") }

    val appointmentMessage by appointmentViewModel.appointmentMessage.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(doctorId) {
        if (doctorId != null && doctorId != -1L) {
            val doctor = doctorRepository.getDoctorById(doctorId)
            selectedDoctor = doctor
        }
    }

    LaunchedEffect(appointmentMessage) {
        appointmentMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            appointmentViewModel.clearMessage()
            if (it.contains("exitosa")) {
                navController.popBackStack()
            }
        }
    }

    val currentCalendar = Calendar.getInstance()
    val year = currentCalendar.get(Calendar.YEAR)
    val month = currentCalendar.get(Calendar.MONTH)
    val day = currentCalendar.get(Calendar.DAY_OF_MONTH)
    val hour = currentCalendar.get(Calendar.HOUR_OF_DAY)
    val minute = currentCalendar.get(Calendar.MINUTE)

    val holidays = remember(year) {
        val dateStrings = listOf(
            "01/01/$year", "18/04/$year", "01/05/$year",
            "28/07/$year", "29/07/$year", "08/10/$year",
            "01/11/$year", "08/12/$year", "25/12/$year"
        )
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dateStrings.mapNotNull { dateString ->
            try {
                formatter.parse(dateString)?.normalized()
            } catch (e: ParseException) {
                Log.e("BookAppointmentScreen", "Error parseando fecha de feriado: $dateString", e)
                null // Si hay error, mapNotNull lo descarta
            }
        }
    }

    val uiDateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
            val tempCalendar = Calendar.getInstance().apply {
                set(selectedYear, selectedMonth, selectedDayOfMonth)
            }
            val tempDate = tempCalendar.time // Esto es un objeto Date

            when {
                tempCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY -> {
                    Toast.makeText(context, "No se pueden reservar citas los domingos.", Toast.LENGTH_LONG).show()
                }
                holidays.any { it == tempDate.normalized() } -> { // Comparamos Date
                    Toast.makeText(context, "No se pueden reservar citas en días feriados.", Toast.LENGTH_LONG).show()
                }
                else -> {
                    selectedDateString = uiDateFormatter.format(tempDate) // Guardamos el string para UI
                }
            }
        }, year, month, day
    )

    datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000 // Para permitir hoy

    val timePickerDialog = TimePickerDialog(
        context,
        { _, selectedHour: Int, selectedMinute: Int ->
            selectedTimeString = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
        }, hour, minute, true
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reservar Cita") },
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
                text = "Detalles de la Cita",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (selectedDoctor == null) {
                Text(
                    text = "Por favor, selecciona un médico desde la lista de 'Médicos Disponibles' o regresa y selecciona 'Ver Médicos' para elegir uno.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Button(
                    onClick = { navController.navigate(AppScreens.DoctorsList) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Seleccionar Médico")
                }
                Spacer(modifier = Modifier.height(24.dp))
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Médico Seleccionado:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        selectedDoctor?.let { doctor ->
                            Text(text = "Nombre: ${doctor.name}")
                            Text(text = "Especialidad: ${doctor.specialty}")
                            Text(text = "Horario: ${doctor.schedule}")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            OutlinedTextField(
                value = selectedDateString,
                onValueChange = { /* No-op */ },
                label = { Text("Fecha de la Cita") },
                readOnly = true,
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = "Fecha") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                trailingIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar fecha")
                    }
                }
            )

            OutlinedTextField(
                value = selectedTimeString,
                onValueChange = { /* No-op */ },
                label = { Text("Hora de la Cita") },
                readOnly = true,
                leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = "Hora") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                trailingIcon = {
                    IconButton(onClick = { timePickerDialog.show() }) {
                        Icon(Icons.Default.Schedule, contentDescription = "Seleccionar hora")
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (userId != -1L && selectedDoctor != null && selectedDateString.isNotBlank() && selectedTimeString.isNotBlank()) {

                        // Ya se validó domingo/feriado al elegir fecha. Solo parsear selectedDateString para el botón.
                        val dateToValidate = try {
                            uiDateFormatter.parse(selectedDateString)?.normalized()
                        } catch (e: ParseException) {
                            null
                        }

                        val calendarToValidate = Calendar.getInstance().apply {
                            dateToValidate?.let { time = it }
                        }

                        when {
                            dateToValidate == null -> {
                                Toast.makeText(context, "Fecha seleccionada no es válida.", Toast.LENGTH_SHORT).show()
                            }
                            calendarToValidate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY -> {
                                Toast.makeText(context, "No se puede confirmar la cita los domingos.", Toast.LENGTH_SHORT).show()
                            }
                            holidays.any { it == dateToValidate } -> {
                                Toast.makeText(context, "No se puede confirmar la cita en un día feriado.", Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                val newAppointment = Appointment(
                                    userId = userId,
                                    doctorId = selectedDoctor!!.id,
                                    date = selectedDateString,
                                    time = selectedTimeString,
                                    isCancelled = false
                                )
                                coroutineScope.launch {
                                    appointmentViewModel.saveAppointment(newAppointment)
                                }
                            }
                        }
                    } else {
                        Toast.makeText(context, "Por favor, completa todos los campos (médico, fecha y hora).", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = selectedDoctor != null && selectedDateString.isNotBlank() && selectedTimeString.isNotBlank()
            ) {
                Text("Confirmar Cita", fontSize = 18.sp)
            }
        }
    }
}