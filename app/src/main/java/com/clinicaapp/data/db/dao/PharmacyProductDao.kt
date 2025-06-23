// com.clinicaapp.data.db.dao.PharmacyProductDao.kt
package com.clinicaapp.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.clinicaapp.data.db.entities.PharmacyProduct
import kotlinx.coroutines.flow.Flow

@Dao
interface PharmacyProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: PharmacyProduct): Long

    @Update
    suspend fun updateProduct(product: PharmacyProduct): Int

    @Delete
    suspend fun deleteProduct(product: PharmacyProduct)

    @Query("SELECT * FROM pharmacy_products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<PharmacyProduct>>

    @Query("SELECT * FROM pharmacy_products WHERE id = :productId")
    suspend fun getProductById(productId: Long): PharmacyProduct?

    @Query("SELECT * FROM pharmacy_products WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchProducts(query: String): Flow<List<PharmacyProduct>>
}