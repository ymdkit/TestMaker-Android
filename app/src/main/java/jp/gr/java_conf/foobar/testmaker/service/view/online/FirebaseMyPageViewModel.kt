package jp.gr.java_conf.foobar.testmaker.service.view.online

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTestResult
import jp.gr.java_conf.foobar.testmaker.service.infra.test.TestMakerRepository

class FirebaseMyPageViewModel(private val repository: TestMakerRepository) : ViewModel() {

    fun fetchMyTests() {
        repository.fetchMyTests()
    }

    fun getMyTests(): LiveData<List<DocumentSnapshot>> {
        return repository.getMyTests()
    }

    fun deleteTest(id: String) {
        repository.deleteTest(id)
    }

    suspend fun downloadTest(id: String): FirebaseTestResult = repository.downloadTest(id)

    fun convert(test: FirebaseTest) {
        repository.createObjectFromFirebase(test)
    }

    fun updateProfile(userName: String, completion: () -> Unit) {
        repository.updateProfile(userName, completion)
    }

}