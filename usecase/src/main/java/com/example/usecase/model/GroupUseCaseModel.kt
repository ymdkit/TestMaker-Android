package com.example.usecase.model

import android.os.Parcelable
import com.example.domain.model.Group
import com.example.domain.model.GroupId
import com.example.domain.model.UserId
import kotlinx.parcelize.Parcelize

@Parcelize
data class GroupUseCaseModel(
    val id: String,
    val name: String,
    val userId: String,
    val createdAt: String
) : Parcelable {
    companion object {
        fun fromGroup(
            group: Group
        ): GroupUseCaseModel =
            GroupUseCaseModel(
                id = group.id.value,
                name = group.name,
                userId = group.userId.value,
                createdAt = group.createdAt
            )
    }

    fun toGroup() = Group(
        id = GroupId(id),
        name = name,
        userId = UserId(userId),
        createdAt = createdAt
    )
}
