package com.example.domain.repository

import com.example.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    val updateUserFlow: Flow<User?>

    suspend fun getUserOrNull(): User?
    suspend fun createUser(user: User)
    suspend fun updateUser(user: User)
    suspend fun logout()
}