package jp.gr.java_conf.foobar.testmaker.service.infra.repository

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import jp.gr.java_conf.foobar.testmaker.service.domain.RealmTest
import jp.gr.java_conf.foobar.testmaker.service.infra.db.LocalDataSource
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTestResult
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.RemoteDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class TestMakerRepository(private val local: LocalDataSource,
                          private val remote: RemoteDataSource) {

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

    suspend fun downloadTest(testId: String): FirebaseTestResult = withContext(Dispatchers.Default) {
        remote.downloadTest(testId)
    }

    fun createObjectFromFirebase(test: FirebaseTest) {
        local.createObjectFromFirebase(test)
    }

    fun updateProfile(userName: String, completion: () -> Unit) {
        remote.updateProfile(userName, completion)
    }

    suspend fun createTest(test: RealmTest, overview: String, isPublic: Boolean = true): String {
        val newDocumentId = withContext(Dispatchers.Default) { remote.createTest(test, overview, isPublic) }
        local.updateDocumentId(getTest(test.id), newDocumentId)
        val newDocumentIds = withContext(Dispatchers.Default) { remote.uploadQuestions(test, newDocumentId) }
        getTest(test.id).questionsNonNull().forEachIndexed { index, quest ->
            local.updateDocumentId(quest, newDocumentIds[index])
        }
        return newDocumentId
    }

    suspend fun createTestInGroup(test: RealmTest, overview: String, groupId: String) = remote.createTest(test, overview, groupId)

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

    fun getTest(testId: Long): RealmTest = local.getTest(testId)

}