package jp.gr.java_conf.foobar.testmaker.service.models

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class TestMakerRepository(private val local: LocalDataSource,
                          private val remote: RemoteDataSource) {

    private var tests: MutableLiveData<List<Test>>? = null

    var questions: MutableLiveData<ArrayList<Quest>>? = null
        private set

    fun getTests(): LiveData<List<Test>> {
        if (tests == null) {
            tests = MutableLiveData()
            fetchTests()
        }
        return tests as LiveData<List<Test>>
    }

    private fun fetchTests() {
        GlobalScope.launch(Dispatchers.Main) {
            tests?.postValue(local.getTests())
        }
    }

    fun getQuestions(testId: Long): LiveData<ArrayList<Quest>> {
        if (questions == null) {
            questions = MutableLiveData()
            fetchQuestions(testId)
        }
        return questions as LiveData<ArrayList<Quest>>
    }

    fun fetchQuestions(testId: Long) {
        GlobalScope.launch(Dispatchers.Main) {
            questions?.postValue(local.getQuestions(testId))
        }
    }

    fun clearQuestions() {
        questions = null
    }

    fun deleteQuestion(question: Quest) {
        local.deleteQuestion(question)
    }

    fun loadImage(imagePath: String, setImage: (Bitmap) -> Unit) {
        local.loadImage(imagePath, setImage)
    }

    fun saveImage(fileName: String, bitmap: Bitmap) {
        local.saveImage(fileName, bitmap)
    }

    fun isAuto(): Boolean {
        return local.isAuto()
    }

    fun isCheckOrder(): Boolean {
        return local.isCheckOrder()
    }

    fun addQuestion(testId: Long, question: LocalQuestion, questionId: Long) {
        local.addQuestion(testId, question, questionId)
    }

    fun getOnlineTests(): LiveData<List<DocumentSnapshot>> {
        return remote.getTests()
    }

    fun downloadQuestions(testId: String) {
        remote.downloadQuestions(testId)
    }

    fun getDownloadQuestions(): LiveData<StructTest> {
        return remote.getDownloadQuestions()
    }

    fun convert(structTest: StructTest, testId: Long) {
        local.convert(structTest, testId)
    }

    fun resetDownloadTest() {
        remote.resetDownloadTest()
    }

    fun fetchOnlineTests() {
        remote.fetchTests()
    }

    fun updateProfile(userName: String, completion: () -> Unit) {
        remote.updateProfile(userName, completion)
    }

    fun createTest(test: Test, overview: String, success: () -> Unit) {
        remote.createTest(test, overview, success)
    }

    fun getMyTests(): LiveData<List<DocumentSnapshot>> {
        return remote.getMyTests()
    }

    fun fetchMyTests() {
        remote.fetchMyTests()
    }

    fun deleteTest(id: String) {
        remote.deleteTest(id)
    }

    fun setUser(user: FirebaseUser?) {
        remote.setUser(user)
    }
}