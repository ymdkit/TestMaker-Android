package jp.gr.java_conf.foobar.testmaker.service.infra.test

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import jp.gr.java_conf.foobar.testmaker.service.domain.Cate
import jp.gr.java_conf.foobar.testmaker.service.domain.Quest
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.db.LocalDataSource
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTestResult
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.RemoteDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class TestMakerRepository(private val local: LocalDataSource,
                          private val remote: RemoteDataSource) {

    var tests: MutableLiveData<List<Test>>? = null
        private set

    var nonCategorizedTests: MutableLiveData<List<Test>>? = null
        private set

    var categorizedTests: MutableLiveData<List<Test>>? = null
        private set

    var questions: MutableLiveData<ArrayList<Quest>>? = null
        private set

    var existingCategories: MutableLiveData<List<Cate>>? = null
        private set


    fun getCategorizedTestsOfLiveData(category: String): LiveData<List<Test>> {
        if (categorizedTests == null) {
            categorizedTests = MutableLiveData()
        }
        fetchCategorisedTests(category)
        return categorizedTests as LiveData<List<Test>>
    }

    fun getTests(): List<Test> = local.getTests()

    fun getTestsOfLiveData(): LiveData<List<Test>> {
        if (tests == null) {
            tests = MutableLiveData()
            fetchTests()
        }
        return tests as LiveData<List<Test>>
    }

    fun getNonCategorizedTestsOfLiveData(): LiveData<List<Test>> {
        if (nonCategorizedTests == null) {
            nonCategorizedTests = MutableLiveData()
            fetchTests()
        }
        return nonCategorizedTests as LiveData<List<Test>>
    }

    fun getExistingCategoriesOfLiveData(): LiveData<List<Cate>> {
        if (existingCategories == null) {
            existingCategories = MutableLiveData()
            fetchCategories()
        }
        return existingCategories as LiveData<List<Cate>>
    }

    fun fetchCategories() {
        GlobalScope.launch(Dispatchers.Main) {
            existingCategories?.postValue(local.getExistingCategories())
        }
    }

    fun fetchTests() {
        GlobalScope.launch(Dispatchers.Main) {
            tests?.postValue(local.getTests())
            nonCategorizedTests?.postValue(local.getNonCategorizedTests())
        }
        fetchCategories()
    }

    fun fetchCategorisedTests(category: String) {
        GlobalScope.launch(Dispatchers.Main) {
            categorizedTests?.postValue(local.getCategorizedTests(category))
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

    fun addQuestion(testId: Long, question: Quest, questionId: Long) {
        local.addQuestion(testId, question, questionId)
    }

    suspend fun downloadTest(testId: String): FirebaseTestResult = remote.downloadTest(testId)

    fun createObjectFromFirebase(test: FirebaseTest) {
        local.createObjectFromFirebase(test)
        fetchTests()
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
    fun deleteCategory(category: Cate) {
        local.deleteCategory(category)
        fetchCategories()
    }

    fun getTest(testId: Long): Test = local.getTest(testId)
    fun getTestClone(testId: Long): Test = local.getTestClone(testId)

    fun addOrUpdateTest(test: Test): Long {
        val id = local.addOrUpdateTest(test)
        fetchTests()
        return id
    }

    fun deleteTest(test: Test) {
        local.deleteTest(test)
        fetchTests()
    }


    fun addQuestions(testId: Long, array: Array<Quest>) = local.addQuestions(testId, array)
    fun deleteQuestions(testId: Long, array: Array<Boolean>) = local.deleteQuestions(testId, array)
    fun resetAchievement(testId: Long) = local.resetAchievement(testId)
    fun resetSolving(testId: Long) = local.resetSolving(testId)
    fun sortManual(from: Int, to: Int, testId: Long) = local.sortManual(from, to, testId)
    fun migrateOrder(testId: Long) = local.migrateOrder(testId)
    fun updateTest(test: Test, title: String, color: Int, category: String) = local.updateTest(test, title, color, category)
    fun updateHistory(test: Test) = local.updateHistory(test)
    fun updateStart(test: Test, start: Int) = local.updateStart(test, start)
    fun updateLimit(test: Test, limit: Int) = local.updateLimit(test, limit)
    fun updateCorrect(quest: Quest, correct: Boolean) = local.updateCorrect(quest, correct)
    fun updateSolving(quest: Quest, solving: Boolean) = local.updateSolving(quest, solving)
    fun getMaxQuestionId(): Long = local.getMaxQuestionId()

}