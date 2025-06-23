package com.clinicaapp.data.db.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

// Asume que tienes una entidad User como esta en alguna parte:
// (Si ya la tienes, asegúrate de que el nombre de la tabla y la columna 'id' coincidan)
//
// @Entity(tableName = "users")
// data class User(
//    @PrimaryKey(autoGenerate = true)
//    val id: Int = 0,
//    // ... otros campos del usuario
// )
//
// Asume que tienes una entidad PharmacyProduct como esta en alguna parte:
//
// @Entity(tableName = "pharmacy_products")
// data class PharmacyProduct(
//    @PrimaryKey(autoGenerate = true)
//    val id: Int = 0,
//    // ... otros campos del producto
// )


// Entidad principal para una compra
@Entity(
    tableName = "purchases",
    foreignKeys = [
        ForeignKey(
            entity = User::class, // Referencia a tu entidad User.kt
            parentColumns = ["id"],   // Columna 'id' en la tabla 'users'
            childColumns = ["userId"],  // Columna 'userId' en la tabla 'purchases'
            onDelete = ForeignKey.CASCADE // Coincide con tu migración
            // onUpdate = ForeignKey.NO_ACTION (es el valor por defecto)
        )
    ],
    indices = [
        Index(value = ["userId"], name = "index_purchases_userId") // Coincide con tu migración
    ]
)
data class Purchase(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,             // Correcto como Int
    val userId: Int,             // Correcto como Int, y ahora con ForeignKey definida
    val purchaseDate: String,
    val totalAmount: Double,
    val paidAmount: Double,
    val changeAmount: Double
)

// Entidad para cada item dentro de una compra
@Entity(
    tableName = "purchase_items",
    primaryKeys = ["purchaseId", "productId"],
    foreignKeys = [
        ForeignKey(
            entity = Purchase::class,
            parentColumns = ["id"],
            childColumns = ["purchaseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PharmacyProduct::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["purchaseId"], name = "index_purchase_items_purchaseId"),
        Index(value = ["productId"], name = "index_purchase_items_productId")
    ]
)
data class PurchaseItem(
    val purchaseId: Int,
    val productId: Int,
    val productName: String,
    val productDescription: String,
    val productPrice: Double,
    val quantity: Int
)

// Clase para la relación entre Purchase y PurchaseItem (para consultas JOIN)
data class PurchaseWithItems(
    @Embedded val purchase: Purchase,
    @Relation(
        parentColumn = "id", // de Purchase
        entityColumn = "purchaseId" // de PurchaseItem
    )
    val items: List<PurchaseItem>
)