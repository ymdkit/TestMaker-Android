package com.example.infra.repository

import com.example.domain.model.User
import com.example.domain.model.UserId
import com.example.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) : UserRepository {

    companion object {
        const val COLLECTION_NAME = "users"
    }

    private val _updateUserFlow: MutableSharedFlow<User?> =
        MutableSharedFlow()
    override val updateUserFlow: Flow<User?>
        get() = _updateUserFlow

    override suspend fun getUserOrNull(): User? = auth.currentUser?.toUser()

    override suspend fun createUser(user: User) {
        db.collection(COLLECTION_NAME)
            .document(user.id.value)
            .set(CreateUserRequest.fromUser(user))
        _updateUserFlow.emit(user)
    }

    override suspend fun updateUser(user: User) {
        db.collection(COLLECTION_NAME)
            .document(user.id.value)
            .set(CreateUserRequest.fromUser(user))
        _updateUserFlow.emit(user)
    }

    override suspend fun logout() {
        auth.signOut()
        _updateUserFlow.emit(null)
    }
}

data class CreateUserRequest(
    val id: String,
    val name: String
) {
    companion object {
        fun fromUser(user: User) =
            CreateUserRequest(
                id = user.id.value,
                name = user.displayName
            )
    }
}

fun FirebaseUser.toUser() = User(
    id = UserId(uid),
    displayName = displayName ?: "Anonymous"
)
