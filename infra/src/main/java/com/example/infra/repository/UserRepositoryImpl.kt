package com.example.infra.repository

import com.example.domain.model.User
import com.example.domain.model.UserId
import com.example.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : UserRepository {

    private val _updateUserFlow: MutableSharedFlow<User?> =
        MutableSharedFlow()
    override val updateUserFlow: Flow<User?>
        get() = _updateUserFlow

    override suspend fun getUserOrNull(): User? = auth.currentUser?.toUser()
}

fun FirebaseUser.toUser() = User(
    id = UserId(uid),
    displayName = displayName ?: "Anonymous"
)
