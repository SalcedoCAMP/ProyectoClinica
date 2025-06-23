package com.clinicaapp.data.db.entities
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val email: String,
    val passwordHash: String,
    val dni: String,
    val role: String = "user"
)