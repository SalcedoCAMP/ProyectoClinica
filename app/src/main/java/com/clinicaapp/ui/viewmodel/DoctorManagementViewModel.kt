package com.clinicaapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.clinicaapp.data.db.entities.Doctor
import com.clinicaapp.data.repository.DoctorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DoctorManagementViewModel(private val doctorRepository: DoctorRepository) : ViewModel() {

    private val _doctors = MutableStateFlow<List<Doctor>>(emptyList())
    val doctors: StateFlow<List<Doctor>> = _doctors.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    // Doctor específico (para edición)
    private val _selectedDoctor = MutableStateFlow<Doctor?>(null)
    val selectedDoctor: StateFlow<Doctor?> = _selectedDoctor.asStateFlow()

    init {
        loadDoctors() // Carga todos los doctores al inicializar el ViewModel
    }

    fun loadDoctors() {
        viewModelScope.launch {
            doctorRepository.getAllDoctors().collect {
                _doctors.value = it
            }
        }
    }

    fun loadDoctorById(id: Long) {
        viewModelScope.launch {
            _selectedDoctor.value = doctorRepository.getDoctorById(id)
        }
    }

    fun addDoctor(doctor: Doctor) {
        viewModelScope.launch {
            try {
                doctorRepository.insertDoctor(doctor)
                _actionMessage.value = "Doctor ${doctor.name} agregado exitosamente."
                loadDoctors() // Recarga la lista tras agregar
            } catch (e: Exception) {
                _actionMessage.value = "Error al agregar doctor: ${e.message}"
            }
        }
    }

    fun updateDoctor(doctor: Doctor) {
        viewModelScope.launch {
            try {
                doctorRepository.updateDoctor(doctor)
                _actionMessage.value = "Doctor ${doctor.name} actualizado exitosamente."
                loadDoctors() // Recarga la lista tras actualizar
            } catch (e: Exception) {
                _actionMessage.value = "Error al actualizar doctor: ${e.message}"
            }
        }
    }

    fun deleteDoctor(doctor: Doctor) {
        viewModelScope.launch {
            try {
                doctorRepository.deleteDoctor(doctor)
                _actionMessage.value = "Doctor ${doctor.name} eliminado exitosamente."
                loadDoctors() // Recargar la lista tras eliminar
            } catch (e: Exception) {
                _actionMessage.value = "Error al eliminar doctor: ${e.message}"
            }
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    fun clearSelectedDoctor() {
        _selectedDoctor.value = null
    }
}

class DoctorManagementViewModelFactory(private val doctorRepository: DoctorRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DoctorManagementViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DoctorManagementViewModel(doctorRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}