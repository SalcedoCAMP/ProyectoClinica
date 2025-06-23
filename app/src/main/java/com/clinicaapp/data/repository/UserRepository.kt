package com.clinicaapp.data.repository

import com.clinicaapp.data.db.dao.UserDao
import com.clinicaapp.data.db.entities.User

class UserRepository(private val userDao: UserDao) {

    /**
     * Registra un nuevo usuario en la base de datos.
     */
    suspend fun registerUser(user: User): Long {
        return userDao.insertUser(user)
    }

    /**
     * Busca un usuario por su correo electrónico y contraseña para el inicio de sesión.
     */
    suspend fun loginUser(email: String, password: String): User? {
        val user = userDao.getUserByEmail(email)
        return if (user != null && user.passwordHash == password) {
            user
        } else {
            null
        }
    }

    /**
     * Obtiene un usuario por su ID.
     */
    suspend fun getUserById(userId: Long): User? {
        return userDao.getUserById(userId)
    }

    /**
     * Obtiene un usuario por su correo electrónico.

     */
    suspend fun getUserByEmail(email: String): User? { // <-- Este método ha sido añadido
        return userDao.getUserByEmail(email)
    }
    /**
     * Actualiza la información de un usuario existente.
     */
    suspend fun updateUser(user: User): Boolean {
        return userDao.updateUser(user) > 0
    }
}