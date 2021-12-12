package jp.gr.java_conf.foobar.testmaker.service.infra.repository

import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import jp.gr.java_conf.foobar.testmaker.service.domain.RealmTest
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.db.LocalDataSource
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.RemoteDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class TestMakerRepository(
    private val local: LocalDataSource,
    private val remote: RemoteDataSource
) {

    suspend fun downloadTest(testId: String): FirebaseTest = withContext(Dispatchers.Default) {
        remote.downloadTest(testId)
    }

    fun createObjectFromFirebase(test: FirebaseTest, source: String) = local.createObjectFromFirebase(test, source = source)

    suspend fun createTest(test: Test, overview: String, isPublic: Boolean) =
        remote.createTest(test, overview, isPublic)


    suspend fun createTestInGroup(test: Test, overview: String, groupId: String) {
        remote.createTest(test = test, overview = overview, isPublic = false, groupId = groupId)
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