package com.clinicaapp.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pharmacy_products")
data class PharmacyProduct(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val description: String,
    val price: Double,
    val stock: Int,
    val imageUrl: String? = null // URL de la imagen del producto (opcional)
)