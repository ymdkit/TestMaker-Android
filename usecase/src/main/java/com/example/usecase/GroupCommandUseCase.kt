package com.example.usecase

import com.example.domain.repository.GroupRepository
import com.example.domain.repository.UserRepository
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
}
