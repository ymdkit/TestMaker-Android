package com.example.usecase

import com.example.domain.repository.UserRepository
import javax.inject.Inject

class UserAuthCommandUseCase @Inject constructor(
    private val repository: UserRepository
) {

    suspend fun registerUser() {
        val user = repository.getUserOrNull() ?: return
        repository.createUser(user)
    }

    suspend fun updateUser(displayName: String) {
        val user = repository.getUserOrNull() ?: return
        repository.updateUser(user.copy(displayName = displayName))
    }


    suspend fun logout() = repository.logout()

}