package com.example.usecase.model

import com.example.domain.model.User
import com.example.domain.model.UserId

data class UserUseCaseModel(
    val id: String,
    val displayName: String
) {

    companion object {
        fun fromUser(user: User) =
            UserUseCaseModel(
                id = user.id.value,
                displayName = user.displayName
            )
    }

    fun toUser() =
        User(
            id = UserId(value = id),
            displayName = displayName
        )
}
