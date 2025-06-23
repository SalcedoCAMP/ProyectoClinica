package com.clinicaapp.data.repository

import com.clinicaapp.data.db.dao.PurchaseDao
import com.clinicaapp.data.db.entities.Purchase
import com.clinicaapp.data.db.entities.PurchaseItem
import com.clinicaapp.data.db.entities.PurchaseWithItems
import kotlinx.coroutines.flow.Flow

class PurchaseRepository(private val purchaseDao: PurchaseDao) {

    suspend fun savePurchase(purchase: Purchase, items: List<PurchaseItem>) {
        purchaseDao.insertPurchaseWithItems(purchase, items)
    }

    fun getAllPurchasesWithItems(): Flow<List<PurchaseWithItems>> {
        return purchaseDao.getAllPurchasesWithItems()
    }

    fun getUserPurchasesWithItems(userId: Long): Flow<List<PurchaseWithItems>> {
        return purchaseDao.getUserPurchasesWithItems(userId)
    }
}