package com.example.domain.repository

import com.example.domain.model.Group
import com.example.domain.model.GroupId
import com.example.domain.model.UserId
import kotlinx.coroutines.flow.Flow

interface GroupRepository {

    val updateGroupListFlow: Flow<List<Group>>

    suspend fun getBelongingGroupList(userId: UserId): List<Group>
    suspend fun createGroup(userId: UserId, groupName: String): Group
    suspend fun joinGroup(userId: UserId, group: Group)
    suspend fun exitGroup(userId: UserId, groupId: GroupId)
    suspend fun deleteGroup(groupId: GroupId)
}