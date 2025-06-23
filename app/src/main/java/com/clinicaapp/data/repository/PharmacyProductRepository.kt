// com.clinicaapp.data.repository.PharmacyProductRepository.kt
package com.clinicaapp.data.repository

import com.clinicaapp.data.db.dao.PharmacyProductDao
import com.clinicaapp.data.db.entities.PharmacyProduct
import kotlinx.coroutines.flow.Flow

class PharmacyProductRepository(private val pharmacyProductDao: PharmacyProductDao) {

    suspend fun saveProduct(product: PharmacyProduct) {
        pharmacyProductDao.insertProduct(product)
    }
    suspend fun insertProduct(product: PharmacyProduct) {
        pharmacyProductDao.insertProduct(product)
    }
    suspend fun updateProduct(product: PharmacyProduct) {
        pharmacyProductDao.updateProduct(product)
    }

    suspend fun deleteProduct(product: PharmacyProduct) {
        pharmacyProductDao.deleteProduct(product)
    }

    fun getAllProducts(): Flow<List<PharmacyProduct>> {
        return pharmacyProductDao.getAllProducts()
    }

    suspend fun getProductById(productId: Long): PharmacyProduct? {
        return pharmacyProductDao.getProductById(productId)
    }

    fun searchProducts(query: String): Flow<List<PharmacyProduct>> {
        return pharmacyProductDao.searchProducts(query)
    }
}