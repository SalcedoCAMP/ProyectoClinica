    package com.clinicaapp.data.db.entities

    import androidx.room.Entity
    import androidx.room.ForeignKey
    import androidx.room.Index
    import androidx.room.PrimaryKey

    @Entity(
        tableName = "appointments",
        foreignKeys = [
            ForeignKey(
                entity = User::class,
                parentColumns = ["id"],
                childColumns = ["userId"],
                onDelete = ForeignKey.CASCADE
            ),
            ForeignKey(
                entity = Doctor::class,
                parentColumns = ["id"],
                childColumns = ["doctorId"],
                onDelete = ForeignKey.CASCADE
            )
        ],
        indices = [
            Index(value = ["userId"]), // Índice para búsquedas rápidas por usuario
            Index(value = ["doctorId"]) // Índice para búsquedas rápidas por médico
        ]
    )
    data class Appointment(
        @PrimaryKey(autoGenerate = true)
        val id: Long = 0L,
        val userId: Long,
        val doctorId: Long,
        val date: String,
        val time: String,
        val isCancelled: Boolean = false //Campo para gestionar cancelaciones
    )