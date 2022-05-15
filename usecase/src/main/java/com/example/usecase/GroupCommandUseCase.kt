package com.example.usecase

import com.example.domain.model.GroupId
import com.example.domain.repository.GroupRepository
import com.example.domain.repository.UserRepository
import com.example.usecase.model.GroupUseCaseModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupCommandUseCase @Inject constructor(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository
) {

    suspend fun createGroup(groupName: String) {
        // todo エラーの伝搬
        val user = userRepository.getUserOrNull() ?: return
        val newGroup = groupRepository.createGroup(
            userId = user.id,
            groupName = groupName
        )
        groupRepository.joinGroup(
            userId = user.id,
            group = newGroup
        )
    }

    suspend fun updateGroup(group: GroupUseCaseModel) {
        val user = userRepository.getUserOrNull() ?: return
        if (group.userId != user.id.value) return

        groupRepository.updateGroup(
            userId = user.id,
            group = group.toGroup()
        )
    }

    suspend fun deleteGroup(group: GroupUseCaseModel) {
        val user = userRepository.getUserOrNull() ?: return
        if (group.userId != user.id.value) return

        groupRepository.deleteGroup(
            userId = user.id,
            groupId = GroupId(group.id)
        )
    }

    suspend fun exitGroup(groupId: String) {
        val user = userRepository.getUserOrNull() ?: return
        groupRepository.exitGroup(
            userId = user.id,
            groupId = GroupId(groupId)
        )
    }

    suspend fun inviteGroup(groupId: String) =
        groupRepository.inviteGroup(groupId = GroupId(groupId))

    suspend fun joinGroup(groupId: String) {
        val user = userRepository.getUserOrNull() ?: return
        val group = groupRepository.getGroupOrNull(groupId = GroupId(groupId)) ?: return
        groupRepository.joinGroup(userId = user.id, group = group)
    }
}
