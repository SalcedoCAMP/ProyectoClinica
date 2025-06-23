package com.clinicaapp.data.repository

import com.clinicaapp.data.db.dao.AppointmentDao
import com.clinicaapp.data.db.entities.Appointment
import kotlinx.coroutines.flow.Flow
import com.clinicaapp.ui.viewmodel.AppointmentWithDoctor


class AppointmentRepository(private val appointmentDao: AppointmentDao) {

    suspend fun saveAppointment(appointment: Appointment) {
        appointmentDao.insertAppointment(appointment)
    }

    suspend fun updateAppointment(appointment: Appointment) {
        appointmentDao.updateAppointment(appointment)
    }

    suspend fun deleteAppointment(appointment: Appointment) {
        appointmentDao.delete(appointment)
    }

    suspend fun getAppointmentById(appointmentId: Long): Appointment? {
        return appointmentDao.getAppointmentById(appointmentId)
    }

    // CAMBIOS AQUÍ para que coincidan con tu DAO
    fun getAppointmentsForUser(userId: Long): Flow<List<AppointmentWithDoctor>> {

        return appointmentDao.getUserAppointmentsWithDoctors(userId)
    }

    // métodos de administrador, directamente del DAO
    fun getAllAppointmentsWithDoctors(): Flow<List<AppointmentWithDoctor>> {
        return appointmentDao.getAllAppointmentsWithDoctors()
    }

    fun getAppointmentsForDoctor(doctorId: Long): Flow<List<AppointmentWithDoctor>> {
        return appointmentDao.getAppointmentsForDoctor(doctorId)
    }

    // Función para cancelar
    suspend fun cancelAppointment(appointmentId: Long) {
        appointmentDao.cancelAppointment(appointmentId)
    }
}