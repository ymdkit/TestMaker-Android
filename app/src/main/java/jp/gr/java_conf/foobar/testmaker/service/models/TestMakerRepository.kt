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

    var questions: MutableLiveData<ArrayList<Quest>>? = null
        private set

    fun getTests(): List<Test> = local.getTests()

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

    fun addQuestion(testId: Long, question: Quest, questionId: Long) {
        local.addQuestion(testId, question, questionId)
    }

    fun downloadQuestions(testId: String) {
        remote.downloadQuestions(testId)
    }

    fun getDownloadQuestions(): LiveData<FirebaseTest> {
        return remote.getDownloadQuestions()
    }

    fun createObjectFromFirebase(test: FirebaseTest) {
        local.createObjectFromFirebase(test)
    }

    fun resetDownloadTest() {
        remote.resetDownloadTest()
    }

    fun updateProfile(userName: String, completion: () -> Unit) {
        remote.updateProfile(userName, completion)
    }

    suspend fun createTest(test: Test, overview: String) {
        remote.createTest(test, overview)
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

    fun getTestsQuery() = remote.getTestsQuery()
    fun getNonCategorizedTests(): List<Test> = local.getNonCategorizedTests()
    fun getExistingCategoryList(): List<Cate> = local.getExistingCategories()
    fun getCategories(): List<Cate> = local.getCategories()
    fun addCategory(category: Cate) = local.addCategory(category)
    fun deleteCategory(category: Cate) = local.deleteCategory(category)
    fun getTest(testId: Long): Test = local.getTest(testId)
    fun getTestClone(testId: Long): Test = local.getTestClone(testId)

    fun addTest(test: Test): Long = local.addTest(test)
    fun addQuestions(testId: Long, array: Array<Quest>) = local.addQuestions(testId, array)
    fun deleteQuestions(testId: Long, array: Array<Boolean>) = local.deleteQuestions(testId, array)


}