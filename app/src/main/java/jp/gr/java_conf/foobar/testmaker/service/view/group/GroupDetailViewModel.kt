package jp.gr.java_conf.foobar.testmaker.service.view.group

import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.domain.Group
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.GroupRepository
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.TestMakerRepository

class GroupDetailViewModel(private val repository: GroupRepository, private val testMakerRepository: TestMakerRepository) : ViewModel() {

    suspend fun getTests(groupId: String) = repository.getTests(groupId)
    suspend fun downloadTest(documentId: String) = repository.downloadTest(documentId)
    fun convert(test: FirebaseTest) = testMakerRepository.createObjectFromFirebase(test)
    fun deleteTest(documentId: String) = testMakerRepository.deleteTest(documentId)

    suspend fun getGroup(groupId: String) = repository.getGroup(groupId)
    suspend fun deleteGroup(groupId: String) = repository.deleteGroup(groupId)
    suspend fun exitGroup(userId: String, groupId: String) = repository.exitGroup(userId, groupId)
    suspend fun joinGroup(userId: String, group: Group) = repository.joinGroup(userId, group)
    suspend fun renameGroup(name: String, group: Group) = repository.updateGroup(group.copy(name = name))

}