package com.clinicaapp.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.clinicaapp.data.db.entities.Appointment
import kotlinx.coroutines.flow.Flow
import com.clinicaapp.ui.viewmodel.AppointmentWithDoctor

@Dao
interface AppointmentDao {

    @Insert
    suspend fun insertAppointment(appointment: Appointment): Long

    @Update
    suspend fun updateAppointment(appointment: Appointment): Int

    @Delete
    suspend fun delete(appointment: Appointment)

    /**
     * Obtiene todas las citas para un usuario específico.

     */
    @Query("SELECT * FROM appointments WHERE userId = :userId ORDER BY date, time DESC")
    fun getUserAppointments(userId: Long): Flow<MutableList<Appointment?>?>?

    /**
     * Marca una cita como cancelada.
     */
    @Query("UPDATE appointments SET isCancelled = 1 WHERE id = :appointmentId")
    suspend fun cancelAppointment(appointmentId: Long)

    // Ya deberías tener este para la pantalla de usuario
   // @Transaction
    @Query("SELECT * FROM appointments WHERE userId = :userId ORDER BY date DESC, time DESC")
    fun getUserAppointmentsWithDoctors(userId: Long): Flow<List<AppointmentWithDoctor>>

    // <-- AÑADE ESTOS MÉTODOS PARA EL ADMINISTRADOR
    //@Transaction
    @Query("SELECT * FROM appointments ORDER BY date DESC, time DESC")
    fun getAllAppointmentsWithDoctors(): Flow<List<AppointmentWithDoctor>>

   // @Transaction
    @Query("SELECT * FROM appointments WHERE doctorId = :doctorId ORDER BY date DESC, time DESC")
    fun getAppointmentsForDoctor(doctorId: Long): Flow<List<AppointmentWithDoctor>>
    // <-- HASTA AQUÍ
    @Query("SELECT * FROM appointments WHERE id = :id LIMIT 1")
    suspend fun getAppointmentById(id: Long): Appointment?

    @Delete // <-- AÑADE ESTO: Método para eliminar una cita
    suspend fun deleteAppointment(appointment: Appointment)
}