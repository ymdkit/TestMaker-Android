package jp.gr.java_conf.foobar.testmaker.service.infra.repository

import com.google.firebase.auth.FirebaseUser
import jp.gr.java_conf.foobar.testmaker.service.domain.Group
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.RemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor(private val dataSource: RemoteDataSource) {

    suspend fun getGroups(userId: String) = dataSource.getGroups(userId)
    suspend fun createGroup(userId: String, groupName: String) =
        dataSource.createGroup(userId, groupName)

    fun createUser(user: FirebaseUser) = dataSource.setUser(user)
    suspend fun getTests(groupId: String) = dataSource.getTests(groupId = groupId)
    suspend fun downloadTest(documentId: String) = dataSource.downloadTest(documentId)

    suspend fun getGroup(groupId: String): Group? = dataSource.getGroup(groupId)
    suspend fun deleteGroup(groupId: String) = dataSource.deleteGroup(groupId)
    suspend fun joinGroup(userId: String, group: Group, groupId: String) =
        dataSource.joinGroup(userId, group.copy(id = groupId))

    suspend fun exitGroup(userId: String, groupId: String) = dataSource.exitGroup(userId, groupId)
    suspend fun updateGroup(group: Group) = dataSource.updateGroup(group)

}