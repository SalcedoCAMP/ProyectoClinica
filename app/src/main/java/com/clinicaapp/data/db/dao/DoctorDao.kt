package com.clinicaapp.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.clinicaapp.data.db.entities.Doctor
import kotlinx.coroutines.flow.Flow

@Dao
interface DoctorDao {

    @Insert
    suspend fun insertDoctor(doctor: Doctor): Long


    @Query("SELECT * FROM doctors ORDER BY name ASC")
    fun getAllDoctors(): Flow<List<Doctor>>

    // CAMBIO AQUÍ: Devuelve Flow<List<Doctor>> directamente
    @Query("SELECT * FROM doctors WHERE specialty = :specialty ORDER BY name ASC")
    fun getDoctorsBySpecialty(specialty: String): Flow<List<Doctor>>

    @Query("SELECT * FROM doctors WHERE id = :doctorId")
    suspend fun getDoctorById(doctorId: Long): Doctor?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(doctor: Doctor)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(doctors: List<Doctor>)

    @Update
    suspend fun update(doctor: Doctor)

    @Delete
    suspend fun delete(doctor: Doctor)
}