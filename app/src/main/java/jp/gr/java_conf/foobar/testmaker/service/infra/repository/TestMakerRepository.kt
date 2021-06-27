package jp.gr.java_conf.foobar.testmaker.service.infra.repository

import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import jp.gr.java_conf.foobar.testmaker.service.domain.RealmTest
import jp.gr.java_conf.foobar.testmaker.service.infra.db.LocalDataSource
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.RemoteDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class TestMakerRepository(private val local: LocalDataSource,
                          private val remote: RemoteDataSource) {

    fun isCheckOrder(): Boolean {
        return local.isCheckOrder()
    }

    suspend fun downloadTest(testId: String): FirebaseTest = withContext(Dispatchers.Default) {
        remote.downloadTest(testId)
    }

    fun createObjectFromFirebase(test: FirebaseTest) {
        local.createObjectFromFirebase(test)
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

    suspend fun createTestInGroup(test: RealmTest, overview: String, groupId: String) {
        val id = remote.createTest(test, overview, groupId)
        remote.uploadQuestions(test, id)
    }

    fun getMyTests(): LiveData<List<DocumentSnapshot>> {
        return remote.getMyTests()
    }

    fun fetchMyTests() {
        remote.fetchMyTests()
    }

    suspend fun deleteTest(id: String) {
        remote.deleteTest(id)
    }

    fun setUser(user: FirebaseUser?) {
        remote.setUser(user)
    }

    fun getTest(testId: Long): RealmTest = local.getTest(testId)

    suspend fun getTestsByUserId(userId: String) = remote.getTestsByUserId(userId)

}