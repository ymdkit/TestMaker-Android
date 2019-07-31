package jp.gr.java_conf.foobar.testmaker.service.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import jp.gr.java_conf.foobar.testmaker.service.models.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.models.Test
import jp.gr.java_conf.foobar.testmaker.service.models.TestMakerRepository

class FirebaseViewModel(private val repository: TestMakerRepository) : ViewModel() {

    init {
        repository.resetDownloadTest()
    }

    fun fetchTests() {
        return repository.fetchOnlineTests()
    }

    fun getTests(): LiveData<List<DocumentSnapshot>> {
        return repository.getOnlineTests()
    }

    fun downloadTest(testId: String) {
        return repository.downloadQuestions(testId)
    }

    fun getDownloadTest(): LiveData<FirebaseTest> {
        return repository.getDownloadQuestions()
    }

    fun convert(test: FirebaseTest) {
        repository.createObjectFromFirebase(test)
    }

    fun createUser(user: FirebaseUser?) {
        repository.setUser(user)
    }

    fun uploadTest(test: Test, overview: String, success: () -> Unit) {
        repository.createTest(test, overview, success)
    }

}