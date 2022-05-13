package com.example.infra.repository

import android.net.Uri
import com.example.domain.model.Group
import com.example.domain.model.GroupId
import com.example.domain.model.UserId
import com.example.domain.repository.GroupRepository
import com.example.infra.remote.DynamicLinksCreator
import com.example.infra.remote.entity.FirebaseGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val dynamicLinksCreator: DynamicLinksCreator
) : GroupRepository {

    companion object {
        const val GROUP_COLLECTION_NAME = "groups"
        const val USER_COLLECTION_NAME = "users"
    }

    private val _updateGroupListFlow: MutableSharedFlow<List<Group>> =
        MutableSharedFlow()
    override val updateGroupListFlow: Flow<List<Group>>
        get() = _updateGroupListFlow

    override suspend fun getBelongingGroupList(userId: UserId): List<Group> =
        db.collection("users")
            .document(userId.value)
            .collection(GROUP_COLLECTION_NAME)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .get()
            .await()
            .toObjects(FirebaseGroup::class.java)
            .map { it.toGroup() }

    override suspend fun createGroup(userId: UserId, groupName: String): Group {
        val ref = db.collection(GROUP_COLLECTION_NAME).document()
        val group = FirebaseGroup(id = ref.id, userId = userId.value, name = groupName)
        ref.set(group).await()

        return group.toGroup()
    }

    override suspend fun updateGroup(userId: UserId, group: Group) {
        db.collection(GROUP_COLLECTION_NAME)
            .document(group.id.value)
            .set(FirebaseGroup.fromGroup(group))
            .await()

        db.collection(USER_COLLECTION_NAME)
            .document(userId.value)
            .collection(GROUP_COLLECTION_NAME)
            .document(group.id.value)
            .set(FirebaseGroup.fromGroup(group))
            .await()

        _updateGroupListFlow.emit(getBelongingGroupList(userId = userId))
    }


    override suspend fun inviteGroup(groupId: GroupId): Uri =
        dynamicLinksCreator.createInviteGroupDynamicLinks(groupId = groupId.value).shortLink
            ?: Uri.EMPTY

    override suspend fun joinGroup(userId: UserId, group: Group) {
        db.collection(USER_COLLECTION_NAME)
            .document(userId.value)
            .collection(GROUP_COLLECTION_NAME)
            .document(group.id.value)
            .set(FirebaseGroup.fromGroup(group))
            .await()
        _updateGroupListFlow.emit(getBelongingGroupList(userId = userId))
    }

    override suspend fun exitGroup(userId: UserId, groupId: GroupId) {
        exit(userId = userId, groupId = groupId)
        _updateGroupListFlow.emit(getBelongingGroupList(userId = userId))
    }

    override suspend fun deleteGroup(userId: UserId, groupId: GroupId) {
        db.collection(GROUP_COLLECTION_NAME)
            .document(groupId.value)
            .delete()
            .await()

        exit(userId = userId, groupId = groupId)
        _updateGroupListFlow.emit(getBelongingGroupList(userId = userId))
    }

    private suspend fun exit(userId: UserId, groupId: GroupId) {
        db.collection(USER_COLLECTION_NAME)
            .document(userId.value)
            .collection(GROUP_COLLECTION_NAME)
            .document(groupId.value)
            .delete()
            .await()
    }

}
