package jp.gr.java_conf.foobar.testmaker.service.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import jp.gr.java_conf.foobar.testmaker.service.models.StructTest
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

    fun getDownloadTest(): LiveData<StructTest> {
        return repository.getDownloadQuestions()
    }

    fun convert(structTest: StructTest) {
        repository.convert(structTest, -1)
    }

    fun updateProfile(userName: String, completion: () -> Unit) {
        repository.updateProfile(userName, completion)
    }

}