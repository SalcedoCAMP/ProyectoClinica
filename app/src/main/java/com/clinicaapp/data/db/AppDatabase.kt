package com.clinicaapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.clinicaapp.data.db.dao.AppointmentDao
import com.clinicaapp.data.db.dao.DoctorDao
import com.clinicaapp.data.db.dao.UserDao
import com.clinicaapp.data.db.dao.PharmacyProductDao
import com.clinicaapp.data.db.dao.PurchaseDao
import com.clinicaapp.data.db.entities.Appointment
import com.clinicaapp.data.db.entities.Doctor
import com.clinicaapp.data.db.entities.User
import com.clinicaapp.data.db.entities.PharmacyProduct
import com.clinicaapp.data.db.entities.Purchase
import com.clinicaapp.data.db.entities.PurchaseItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        Doctor::class,
        Appointment::class,
        PharmacyProduct::class,
        Purchase::class,
        PurchaseItem::class
    ],
    version = 5, // <--- VERSIÓN INCREMENTADA A 5
    exportSchema = false // Puedes cambiarlo a true si deseas exportar el esquema para control de versiones
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun doctorDao(): DoctorDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun pharmacyProductDao(): PharmacyProductDao
    abstract fun purchaseDao(): PurchaseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "clinica_database"
                )
                    .addCallback(AppDatabaseCallback(context))
                    // Asegúrate de que tus migraciones anteriores y las nuevas estén en orden.
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5 // <--- AÑADIDA NUEVA MIGRACIÓN
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Migración existente de 1 a 2
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN role TEXT NOT NULL DEFAULT 'user'")
            }
        }

        // NUEVA MIGRACIÓN DE 2 A 3: Crea la tabla pharmacy_products
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE pharmacy_products (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT NOT NULL,
                        price REAL NOT NULL,
                        stock INTEGER NOT NULL,
                        imageUrl TEXT
                    )
                    """
                )
            }
        }

        // NUEVA MIGRACIÓN DE 3 A 4: Crea las tablas purchases y purchase_items
        // Esta migración ya crea las tablas con las claves foráneas e índices correctos.
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE purchases (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        purchaseDate TEXT NOT NULL,
                        totalAmount REAL NOT NULL,
                        paidAmount REAL NOT NULL,
                        changeAmount REAL NOT NULL,
                        FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE
                    )
                    """
                )
                db.execSQL(
                    """
                    CREATE TABLE purchase_items (
                        purchaseId INTEGER NOT NULL,
                        productId INTEGER NOT NULL,
                        productName TEXT NOT NULL,
                        productDescription TEXT NOT NULL,
                        productPrice REAL NOT NULL,
                        quantity INTEGER NOT NULL,
                        PRIMARY KEY(purchaseId, productId),
                        FOREIGN KEY(purchaseId) REFERENCES purchases(id) ON DELETE CASCADE,
                        FOREIGN KEY(productId) REFERENCES pharmacy_products(id) ON DELETE CASCADE
                    )
                    """
                )
                // Añadir índices para las claves foráneas si no están implícitamente creados por las FK
                db.execSQL("CREATE INDEX IF NOT EXISTS index_purchases_userId ON purchases (userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_purchase_items_purchaseId ON purchase_items (purchaseId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_purchase_items_productId ON purchase_items (productId)")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {

            }
        }


        private class AppDatabaseCallback(private val context: Context) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                CoroutineScope(Dispatchers.IO).launch {

                    val userDao = getDatabase(context).userDao()
                    val doctorDao = getDatabase(context).doctorDao()
                    val pharmacyProductDao = getDatabase(context).pharmacyProductDao()

                    // Solo inserta si no existen para evitar duplicados en recreaciones
                    if (userDao.getUserByEmail("admin@clinica.com") == null) {
                        val adminUser = User(
                            name = "Admin General",
                            email = "admin@clinica.com",
                            passwordHash = "adminpass", // Considera hashear esto de forma segura
                            dni = "99999999",
                            role = "admin"
                        )
                        userDao.insertUser(adminUser)
                    }

                    val doctors = listOf(
                        Doctor(name = "Dr. Ana Gómez", specialty = "Pediatría", schedule = "L-V 9-17"),
                        Doctor(name = "Dra. Laura Flores", specialty = "Dermatología", schedule = "M-J 10-18"),
                        Doctor(name = "Dr. Carlos Ruiz", specialty = "Cardiología", schedule = "L-V 8-16")
                    )
                    // Idealmente, también verificarías si ya existen para evitar duplicados
                    doctors.forEach { doctor -> doctorDao.insertDoctor(doctor) }

                    val products = listOf(
                        PharmacyProduct(name = "Paracetamol 500mg", description = "Analgésico y antipirético", price = 5.50, stock = 100),
                        PharmacyProduct(name = "Ibuprofeno 400mg", description = "Antiinflamatorio no esteroideo", price = 8.75, stock = 75),
                    )
                    // Idealmente, también verificarías si ya existen
                    products.forEach { product ->
                        pharmacyProductDao.insertProduct(product)
                    }
                }
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)

            }
        }
    }
}