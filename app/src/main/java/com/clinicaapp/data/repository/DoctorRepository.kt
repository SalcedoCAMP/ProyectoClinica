package com.clinicaapp.data.repository

import kotlinx.coroutines.flow.Flow // Necesario para devolver Flow
import kotlinx.coroutines.flow.map
import com.clinicaapp.data.db.dao.DoctorDao
import com.clinicaapp.data.db.entities.Doctor

// No necesitas estos imports si ya no usas stateIn aquí
// import kotlinx.coroutines.flow.StateFlow
// import kotlinx.coroutines.flow.stateIn
// import kotlinx.coroutines.CoroutineScope
// import kotlinx.coroutines.Dispatchers
// import kotlinx.coroutines.SupervisorJob
// import kotlinx.coroutines.flow.emptyFlow


class DoctorRepository(private val doctorDao: DoctorDao) {

    fun getAllDoctors(): Flow<List<Doctor>> {
        return doctorDao.getAllDoctors()
    }

    /**
     * Obtiene médicos filtrados por especialidad.
     */
    fun getDoctorsBySpecialty(specialty: String): Flow<List<Doctor>> {

        return doctorDao.getDoctorsBySpecialty(specialty)
    }

    /**
     * Obtiene un médico por su ID.
     */
    suspend fun getDoctorById(doctorId: Long): Doctor? {
        return doctorDao.getDoctorById(doctorId)
    }

    suspend fun insertDoctor(doctor: Doctor) {
        doctorDao.insert(doctor)
    }

    suspend fun updateDoctor(doctor: Doctor) {
        doctorDao.update(doctor)
    }

    suspend fun deleteDoctor(doctor: Doctor) {
        doctorDao.delete(doctor)
    }
}