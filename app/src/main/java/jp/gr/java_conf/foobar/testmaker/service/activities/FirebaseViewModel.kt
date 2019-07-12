package jp.gr.java_conf.foobar.testmaker.service.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import jp.gr.java_conf.foobar.testmaker.service.models.StructTest
import jp.gr.java_conf.foobar.testmaker.service.models.TestMakerRepository

class FirebaseViewModel(private val repository: TestMakerRepository) : ViewModel() {

    init {
        repository.resetDownloadTest()
    }

    fun fetchTests(){
        return repository.fetchOnlineTests()
    }

    fun getTests(): LiveData<List<DocumentSnapshot>> {
        return repository.getOnlineTests()
    }

    fun downloadTest(testId: String) {
        return repository.downloadQuestions(testId)
    }

    fun getDownloadTest(): LiveData<StructTest> {
        return repository.getDownloadQuestions()
    }

    fun convert(structTest: StructTest) {
        repository.convert(structTest,-1)
    }

}