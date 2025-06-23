package com.clinicaapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.clinicaapp.data.db.entities.Doctor
import com.clinicaapp.data.repository.DoctorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted // Importar esto
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn // Importar esto

class DoctorViewModel(private val doctorRepository: DoctorRepository) : ViewModel() {

    private val _allDoctors = MutableStateFlow<List<Doctor>>(emptyList())

    private val _selectedSpecialty = MutableStateFlow<String?>(null)
    val selectedSpecialty: StateFlow<String?> = _selectedSpecialty.asStateFlow()

    // Lista de doctores filtrados que se mostrará en la UI.
    val doctors: StateFlow<List<Doctor>> = combine(
        _allDoctors,
        _selectedSpecialty
    ) { allDoctors, selectedSpecialty ->
        if (selectedSpecialty.isNullOrBlank() || selectedSpecialty == "Todas") {
            allDoctors
        } else {
            allDoctors.filter { it.specialty == selectedSpecialty }
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList() // Valor inicial
        )


    val specialties: StateFlow<List<String>> = _allDoctors
        .map { doctorsList ->
            doctorsList.map { it.specialty }.distinct().sorted()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList() // Valor inicial
        )

    init {
        loadAllDoctors()
    }

    private fun loadAllDoctors() {
        viewModelScope.launch {
            doctorRepository.getAllDoctors().collect { fetchedDoctors ->
                _allDoctors.value = fetchedDoctors
            }
        }
    }

    fun filterDoctorsBySpecialty(specialty: String?) {
        _selectedSpecialty.value = specialty
    }
}

// Factory para inyectar DoctorRepository
class DoctorViewModelFactory(private val doctorRepository: DoctorRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DoctorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DoctorViewModel(doctorRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}