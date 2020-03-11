package jp.gr.java_conf.foobar.testmaker.service.infra.repository

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import jp.gr.java_conf.foobar.testmaker.service.domain.Quest
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.db.LocalDataSource
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTestResult
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.RemoteDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class TestMakerRepository(private val local: LocalDataSource,
                          private val remote: RemoteDataSource) {

    var questions: MutableLiveData<ArrayList<Quest>>? = null
        private set

    fun getQuestions(testId: Long): LiveData<ArrayList<Quest>> {
        if (questions == null) {
            questions = MutableLiveData()
            fetchQuestions(testId)
        }
        return questions as LiveData<ArrayList<Quest>>
    }

    fun fetchQuestions(testId: Long) {
        questions?.value = local.getQuestions(testId)
    }

    fun clearQuestions() {
        questions = null
    }

    fun deleteQuestion(question: Quest) {
        local.deleteQuestion(question)
    }

    suspend fun loadImage(imagePath: String): Bitmap? = withContext(Dispatchers.IO) {
        local.loadImage(imagePath)
    }

    suspend fun saveImage(fileName: String, bitmap: Bitmap) = withContext(Dispatchers.IO) {
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

    suspend fun downloadTest(testId: String): FirebaseTestResult = withContext(Dispatchers.Default) {
        remote.downloadTest(testId)
    }

    fun createObjectFromFirebase(test: FirebaseTest) {
        local.createObjectFromFirebase(test)
    }

    fun updateProfile(userName: String, completion: () -> Unit) {
        remote.updateProfile(userName, completion)
    }

    suspend fun createTest(test: Test, overview: String, oldDocumentId: String): String {
        val newDocumentId = withContext(Dispatchers.Default) { remote.createTest(test, overview, oldDocumentId) }
        local.updateDocumentId(getTest(test.id), newDocumentId)
        val newDocumentIds = withContext(Dispatchers.Default) { remote.uploadQuestions(test, newDocumentId) }
        getTest(test.id).questionsNonNull().forEachIndexed { index, quest ->
            local.updateDocumentId(quest, newDocumentIds[index])
        }
        return newDocumentId
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

    fun getTest(testId: Long): Test = local.getTest(testId)

    fun addQuestions(testId: Long, array: Array<Quest>) = local.addQuestions(testId, array)
    fun deleteQuestions(testId: Long, array: Array<Boolean>) = local.deleteQuestions(testId, array)
    fun resetSolving(testId: Long) = local.resetSolving(testId)
    fun sortManual(from: Int, to: Int, testId: Long) = local.sortManual(from, to, testId)
    fun migrateOrder(testId: Long) = local.migrateOrder(testId)

    fun updateHistory(test: Test) = local.updateHistory(test)
    fun updateStart(test: Test, start: Int) = local.updateStart(test, start)
    fun updateLimit(test: Test, limit: Int) = local.updateLimit(test, limit)
    fun updateCorrect(quest: Quest, correct: Boolean) = local.updateCorrect(quest, correct)
    fun updateSolving(quest: Quest, solving: Boolean) = local.updateSolving(quest, solving)
    fun getMaxQuestionId(): Long = local.getMaxQuestionId()

}