package com.clinicaapp.ui.viewmodel

import androidx.room.Embedded
import androidx.room.Relation
import com.clinicaapp.data.db.entities.Appointment
import com.clinicaapp.data.db.entities.Doctor

data class AppointmentWithDoctor(
    @Embedded val appointment: Appointment,
    @Relation(
        parentColumn = "doctorId", // Entidad Appointment
        entityColumn = "id"        // Entidad Doctor
    )
    val doctor: Doctor
)