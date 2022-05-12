package com.example.usecase.model

import com.example.domain.model.Group
import com.example.domain.model.UserId

data class GroupUseCaseModel(
    val id: String,
    val name: String,
    val userId: UserId,
    val createdAt: String
) {
    companion object {
        fun fromGroup(
            group: Group
        ): GroupUseCaseModel =
            GroupUseCaseModel(
                id = group.id.value,
                name = group.name,
                userId = group.userId,
                createdAt = group.createdAt
            )
    }
}
