package jp.gr.java_conf.foobar.testmaker.service.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import jp.gr.java_conf.foobar.testmaker.service.models.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.models.TestMakerRepository

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

    fun downloadTest(id: String) {
        repository.downloadQuestions(id)
    }

    fun getDownloadTest(): LiveData<FirebaseTest> {
        return repository.getDownloadQuestions()
    }

    fun convert(test: FirebaseTest) {
        repository.createObjectFromFirebase(test)
    }

    fun updateProfile(userName: String, completion: () -> Unit) {
        repository.updateProfile(userName, completion)
    }

}