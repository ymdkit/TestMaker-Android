package jp.gr.java_conf.foobar.testmaker.service.infra.test

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import jp.gr.java_conf.foobar.testmaker.service.domain.Cate
import jp.gr.java_conf.foobar.testmaker.service.domain.Quest
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.infra.db.LocalDataSource
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTestResult
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.RemoteDataSource
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

    suspend fun downloadTest(testId: String): FirebaseTestResult = remote.downloadTest(testId)

    fun createObjectFromFirebase(test: FirebaseTest) {
        local.createObjectFromFirebase(test)
    }

    fun updateProfile(userName: String, completion: () -> Unit) {
        remote.updateProfile(userName, completion)
    }

    suspend fun createTest(test: Test, overview: String): String {
        return remote.createTest(test, overview)
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
    fun getCategorizedTests(category: String): List<Test> = local.getCategorizedTests(category)
    fun getNonCategorizedTests(): List<Test> = local.getNonCategorizedTests()
    fun getExistingCategoryList(): List<Cate> = local.getExistingCategories()
    fun getCategories(): List<Cate> = local.getCategories()
    fun addCategory(category: Cate) = local.addCategory(category)
    fun deleteCategory(category: Cate) = local.deleteCategory(category)
    fun getTest(testId: Long): Test = local.getTest(testId)
    fun getTestClone(testId: Long): Test = local.getTestClone(testId)

    fun addOrUpdateTest(test: Test): Long = local.addOrUpdateTest(test)
    fun addQuestions(testId: Long, array: Array<Quest>) = local.addQuestions(testId, array)
    fun deleteQuestions(testId: Long, array: Array<Boolean>) = local.deleteQuestions(testId, array)
    fun resetAchievement(testId: Long) = local.resetAchievement(testId)
    fun resetSolving(testId: Long) = local.resetSolving(testId)
    fun sortManual(from: Int, to: Int, testId: Long) = local.sortManual(from, to, testId)
    fun migrateOrder(testId: Long) = local.migrateOrder(testId)
    fun updateTest(test: Test, title: String, color: Int, category: String) = local.updateTest(test, title, color, category)
    fun deleteTest(test: Test) = local.deleteTest(test)
    fun updateHistory(test: Test) = local.updateHistory(test)
    fun updateStart(test: Test, start: Int) = local.updateStart(test, start)
    fun updateLimit(test: Test, limit: Int) = local.updateLimit(test, limit)
    fun updateCorrect(quest: Quest, correct: Boolean) = local.updateCorrect(quest, correct)
    fun updateSolving(quest: Quest, solving: Boolean) = local.updateSolving(quest, solving)
    fun getMaxQuestionId(): Long = local.getMaxQuestionId()
}