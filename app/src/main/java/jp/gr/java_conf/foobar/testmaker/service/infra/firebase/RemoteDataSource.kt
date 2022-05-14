package jp.gr.java_conf.foobar.testmaker.service.infra.firebase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.infra.remote.entity.FirebaseHistory
import com.example.infra.remote.entity.FirebaseQuestion
import com.example.infra.remote.entity.FirebaseTest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDataSource @Inject constructor(
    @ApplicationContext val context: Context,
    val auth: Auth
) {

    private val db = FirebaseFirestore.getInstance()

    suspend fun downloadTest(testId: String): FirebaseTest =
        db.collection(TESTS)
            .document(testId)
            .get().await()
            .toObject(FirebaseTest::class.java)?.apply {
                documentId = testId
                questions = downloadQuestions(testId)
            } ?: throw Exception()

    private suspend fun downloadQuestions(testId: String): List<FirebaseQuestion> {
        return db.collection(TESTS)
            .document(testId)
            .collection("questions")
            .get()
            .await()
            .toObjects(FirebaseQuestion::class.java).sortedBy { q -> q.order }
    }

    suspend fun createHistory(documentId: String, history: FirebaseHistory) {
        val ref = db.collection(TESTS)
            .document(documentId)
            .collection("histories")
            .document()
        ref.set(history.copy(id = ref.id)).await()
    }


    // todo 画像のアップロード
    private fun uploadImage(localPath: String, remotePath: String): String {
        val storage = FirebaseStorage.getInstance()

        val storageRef = storage.reference.child(remotePath)

        val baos = ByteArrayOutputStream()
        val imageOptions = BitmapFactory.Options()
        imageOptions.inPreferredConfig = Bitmap.Config.RGB_565
        runCatching {
            context.openFileInput(localPath)
        }.onSuccess {
            val bitmap = BitmapFactory.decodeStream(it, null, imageOptions)
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 50, baos)
            val data = baos.toByteArray()
            storageRef.putBytes(data)
            return remotePath
        }.onFailure {
            return ""
        }
        return ""
    }

    companion object {
        const val TESTS = "tests"
        const val GROUPS = "groups"
        const val USERS = "users"
        const val HISTORIES = "histories"
        const val QUESTIONS = "questions"
    }

    sealed class FirebasePostResponse {
        object Divided : FirebasePostResponse()
        data class Failure(val message: String?) : FirebasePostResponse()
        data class Success(val documentId: String) : FirebasePostResponse()
    }

}
