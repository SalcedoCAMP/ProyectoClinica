// com.clinicaapp.data.db.dao.PurchaseItemDao.kt
package com.clinicaapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.clinicaapp.data.db.entities.PurchaseItem

@Dao
interface PurchaseItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(purchaseItems: List<PurchaseItem>)

    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    fun getItemsForPurchase(purchaseId: Long): List<PurchaseItem>
}