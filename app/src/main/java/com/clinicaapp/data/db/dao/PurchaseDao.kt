package com.clinicaapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.clinicaapp.data.db.entities.Purchase
import com.clinicaapp.data.db.entities.PurchaseItem
import com.clinicaapp.data.db.entities.PurchaseWithItems
import kotlinx.coroutines.flow.Flow


@Dao
interface PurchaseDao {

    @Insert
    suspend fun insertPurchase(purchase: Purchase): Long

    @Insert
    suspend fun insertPurchaseItem(item: PurchaseItem)

    @Transaction
    suspend fun insertPurchaseWithItems(purchase: Purchase, items: List<PurchaseItem>) {
        val purchaseId = insertPurchase(purchase)
        items.forEach { item ->
            insertPurchaseItem(item.copy(purchaseId = purchaseId.toInt()))
        }
    }

    @Transaction
    @Query("SELECT * FROM purchases ORDER BY purchaseDate DESC")
    fun getAllPurchasesWithItems(): Flow<List<PurchaseWithItems>>

    @Transaction
    @Query("SELECT * FROM purchases WHERE userId = :userId ORDER BY purchaseDate DESC")
    fun getUserPurchasesWithItems(userId: Long): Flow<List<PurchaseWithItems>>
}