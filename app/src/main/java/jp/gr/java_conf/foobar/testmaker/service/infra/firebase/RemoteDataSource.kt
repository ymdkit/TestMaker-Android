package jp.gr.java_conf.foobar.testmaker.service.infra.firebase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.infra.remote.entity.FirebaseHistory
import com.example.infra.remote.entity.FirebaseQuestion
import com.example.infra.remote.entity.FirebaseTest
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.*
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

    suspend fun getHistories(documentId: String) =
        db.collection(TESTS)
            .document(documentId)
            .collection("histories")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .get()
            .await()
            .toObjects(FirebaseHistory::class.java)

    suspend fun createHistory(documentId: String, history: FirebaseHistory) {
        val ref = db.collection(TESTS)
            .document(documentId)
            .collection("histories")
            .document()
        ref.set(history.copy(id = ref.id)).await()
    }

    suspend fun createTest(
        test: Test,
        overview: String,
        isPublic: Boolean,
        groupId: String = ""
    ): FirebasePostResponse {
        val user = auth.getUser() ?: return FirebasePostResponse.Failure("user does not exist")

        val batchOperationLimit = 500

        if (test.questions.size >= batchOperationLimit) {
            val result = runCatching {
                test.questions.sortedBy { it.order }.chunked(batchOperationLimit - 1)
                    .forEachIndexed { index, it ->
                        val testRef = db.collection(TESTS).document()

                        db.runBatch { batch ->
                            batch.set(
                                testRef,
                                testToFirebaseTest(
                                    test = test.copy(title = if (index == 0) test.title else "${test.title}($index)"),
                                    user = user,
                                    overview = overview,
                                    isPublic = isPublic,
                                    size = it.size,
                                    groupId = groupId,
                                )
                            )
                            it.forEach {
                                val questionRef =
                                    db.collection(TESTS).document(testRef.id).collection(QUESTIONS)
                                        .document()

                                val imageUrl = if (it.hasLocalImage) {
                                    uploadImage(it.imagePath, "${user.uid}/${it.imagePath}")
                                } else ""

                                batch.set(questionRef, it.toFirebaseQuestion(imageUrl = imageUrl))
                            }
                        }.await()
                    }
            }

            return if (result.isSuccess) {
                FirebasePostResponse.Divided
            } else {
                FirebasePostResponse.Failure(result.exceptionOrNull()?.message)
            }
        } else {
            val testRef = db.collection(TESTS).document()
            val result = runCatching {

                db.runBatch { batch ->
                    batch.set(
                        testRef,
                        testToFirebaseTest(
                            test = test,
                            user = user,
                            overview = overview,
                            isPublic = isPublic,
                            size = test.questions.size,
                            groupId = groupId
                        )
                    )
                    test.questions.forEach {
                        val questionRef =
                            db.collection(TESTS).document(testRef.id).collection(QUESTIONS)
                                .document()

                        val imageUrl = if (it.hasLocalImage) {
                            uploadImage(it.imagePath, "${user.uid}/${it.imagePath}")
                        } else ""

                        batch.set(questionRef, it.toFirebaseQuestion(imageUrl = imageUrl))
                    }
                }.await()

            }

            return if (result.isSuccess) {
                FirebasePostResponse.Success(testRef.id)
            } else {
                FirebasePostResponse.Failure(result.exceptionOrNull()?.message)
            }

        }
    }

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

    private fun testToFirebaseTest(
        test: Test,
        user: FirebaseUser,
        overview: String,
        isPublic: Boolean,
        size: Int,
        groupId: String
    ) = FirebaseTest(
        userId = user.uid,
        userName = user.displayName ?: "Anonymous",
        overview = overview,
        size = size,
        locale = Locale.getDefault().language,
        public = isPublic,
        name = test.title,
        color = test.getColorId(context),
        groupId = groupId
    )

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
