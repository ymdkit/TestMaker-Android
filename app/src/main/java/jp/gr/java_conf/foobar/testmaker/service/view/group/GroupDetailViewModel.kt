package jp.gr.java_conf.foobar.testmaker.service.view.group

import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.GroupRepository
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.TestMakerRepository

class GroupDetailViewModel(private val repository: GroupRepository, private val testMakerRepository: TestMakerRepository) : ViewModel() {

    suspend fun getTests(groupId: String) = repository.getTests(groupId)
    suspend fun downloadTest(documentId: String) = repository.downloadTest(documentId)
    fun convert(test: FirebaseTest) = testMakerRepository.createObjectFromFirebase(test)
    fun deleteTest(documentId: String) = testMakerRepository.deleteTest(documentId)

}