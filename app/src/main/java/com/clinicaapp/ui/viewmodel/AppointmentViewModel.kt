package com.clinicaapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.clinicaapp.data.db.entities.Appointment
import com.clinicaapp.data.repository.AppointmentRepository
import com.clinicaapp.data.repository.DoctorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.clinicaapp.ui.viewmodel.AppointmentWithDoctor


class AppointmentViewModel(
    private val appointmentRepository: AppointmentRepository,
    private val doctorRepository: DoctorRepository
) : ViewModel() {

    private val _allAppointments = MutableStateFlow<List<AppointmentWithDoctor>>(emptyList())
    val allAppointments: StateFlow<List<AppointmentWithDoctor>> = _allAppointments.asStateFlow()

    private val _appointmentMessage = MutableStateFlow<String?>(null)
    val appointmentMessage: StateFlow<String?> = _appointmentMessage.asStateFlow()

    private val _selectedDoctorId = MutableStateFlow<Long?>(null)

    private val _userAppointments = MutableStateFlow<List<AppointmentWithDoctor>>(emptyList())
    val userAppointments: StateFlow<List<AppointmentWithDoctor>> = _userAppointments.asStateFlow()

    private val _currentUserId = MutableStateFlow<Long?>(null)


    init {
        viewModelScope.launch {
            _selectedDoctorId.collectLatest { doctorId ->
                if (doctorId != null && doctorId != -1L) {
                    appointmentRepository.getAppointmentsForDoctor(doctorId)
                        .collect { appointmentsWithDoctors ->
                            _allAppointments.value = appointmentsWithDoctors
                        }
                } else {
                    appointmentRepository.getAllAppointmentsWithDoctors()
                        .collect { appointmentsWithDoctors ->
                            _allAppointments.value = appointmentsWithDoctors
                        }
                }
            }
        }

        viewModelScope.launch {
            _currentUserId.collectLatest { userId ->
                if (userId != null && userId != -1L) {
                    appointmentRepository.getAppointmentsForUser(userId)
                        .collect { appointmentsWithDoctors ->
                            _userAppointments.value = appointmentsWithDoctors
                        }
                } else {
                    _userAppointments.value = emptyList()
                }
            }
        }
    }

    fun loadAllAppointments() {
        _selectedDoctorId.value = null
    }

    fun loadAppointmentsForDoctor(doctorId: Long) {
        _selectedDoctorId.value = doctorId
    }

    fun loadUserAppointments(userId: Long) {
        _currentUserId.value = userId
    }

    suspend fun saveAppointment(appointment: Appointment) {
        viewModelScope.launch {
            try {
                appointmentRepository.saveAppointment(appointment)
                _appointmentMessage.value = "Cita agendada exitosamente."
            } catch (e: Exception) {
                _appointmentMessage.value = "Error al agendar la cita: ${e.message}"
            }
        }
    }

    fun clearMessage() {
        _appointmentMessage.value = null
    }

    fun cancelAppointment(appointmentId: Long) {
        viewModelScope.launch {
            try {
                appointmentRepository.cancelAppointment(appointmentId)
                _appointmentMessage.value = "Cita cancelada exitosamente."
            } catch (e: Exception) {
                _appointmentMessage.value = "Error al cancelar la cita: ${e.message}"
            }
        }
    }


    fun deleteAppointmentById(appointmentId: Long) {
        viewModelScope.launch {
            try {
                val appointment = appointmentRepository.getAppointmentById(appointmentId)
                if (appointment != null) {
                    appointmentRepository.deleteAppointment(appointment)
                    _appointmentMessage.value = "Cita eliminada exitosamente."
                } else {
                    _appointmentMessage.value = "Error: Cita no encontrada."
                }
            } catch (e: Exception) {
                _appointmentMessage.value = "Error al eliminar la cita: ${e.message}"
            }
        }
    }
}

class AppointmentViewModelFactory(
    private val appointmentRepository: AppointmentRepository,
    private val doctorRepository: DoctorRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppointmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppointmentViewModel(appointmentRepository, doctorRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}