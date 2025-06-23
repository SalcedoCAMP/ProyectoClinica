package com.clinicaapp.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "doctors")
data class Doctor(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val specialty: String,
    val schedule: String
)
