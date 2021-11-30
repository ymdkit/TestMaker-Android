package jp.gr.java_conf.foobar.testmaker.service.infra.firebase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import jp.gr.java_conf.foobar.testmaker.service.domain.Group
import jp.gr.java_conf.foobar.testmaker.service.domain.History
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.*


class RemoteDataSource(val context: Context, val auth: Auth) {

    private var myTests: MutableLiveData<List<DocumentSnapshot>>? = null

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

    fun setUser(user: FirebaseUser?) {

        user ?: return

        val myUser = MyFirebaseUser(
            name = user.displayName
                ?: "guest", id = user.uid
        )

        db.collection("users")
            .document(user.uid)
            .set(myUser)

    }

    fun getMyTests(): LiveData<List<DocumentSnapshot>> {
        if (myTests == null) {
            myTests = MutableLiveData()
            fetchMyTests()
        }
        return myTests as LiveData<List<DocumentSnapshot>>
    }

    fun fetchMyTests() {

        val user = auth.getUser() ?: return

        db.collection(TESTS)
            .whereEqualTo("userId", user.uid)
            .orderBy("created_at", Query.Direction.DESCENDING)
            .limit(300)
            .get()
            .addOnSuccessListener { query ->

                myTests?.postValue(query.documents)

            }
            .addOnFailureListener {
                Log.d("Debug", "fetch myTests failure: $it")
            }
    }

    suspend fun getGroups(userId: String): List<Group> = db.collection("users")
        .document(userId)
        .collection("groups")
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .limit(100)
        .get()
        .await()
        .toObjects(Group::class.java)

    suspend fun createGroup(userId: String, groupName: String): Group {
        val ref = db.collection("groups").document()
        val group = Group(id = ref.id, userId = userId, name = groupName)
        ref.set(group).await()
        return group
    }

    suspend fun deleteTest(documentId: String) =
        db.collection(TESTS)
            .document(documentId)
            .delete()
            .await()

    suspend fun joinGroup(userId: String, group: Group) =
        db.collection("users")
            .document(userId)
            .collection("groups")
            .document(group.id)
            .set(group)
            .await()

    suspend fun exitGroup(userId: String, groupId: String) =
        db.collection("users")
            .document(userId)
            .collection("groups")
            .document(groupId)
            .delete()
            .await()

    suspend fun getTests(groupId: String) =
        db.collection(TESTS)
            .whereEqualTo("groupId", groupId)
            .orderBy("created_at", Query.Direction.DESCENDING)
            .limit(100)
            .get()
            .await()
            .documents

    suspend fun getGroup(groupId: String) =
        db.collection("groups")
            .document(groupId)
            .get()
            .await()
            .toObject(Group::class.java)
            ?.copy(id = groupId)

    suspend fun updateGroup(group: Group) =
        db.collection("groups")
            .document(group.id)
            .set(group)
            .await()

    suspend fun deleteGroup(documentId: String) =
        db.collection("groups")
            .document(documentId)
            .delete()
            .await()

    suspend fun getHistories(documentId: String) =
        db.collection(TESTS)
            .document(documentId)
            .collection("histories")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(100)
            .get()
            .await()
            .toObjects(History::class.java)

    suspend fun createHistory(documentId: String, history: History) {
        val ref = db.collection(TESTS)
            .document(documentId)
            .collection("histories")
            .document()
        ref.set(history.copy(id = ref.id)).await()
    }

    suspend fun getTestsByUserId(userId: String) =
        db.collection(TESTS)
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .map {
                it.toObject(FirebaseTest::class.java).copy(documentId = it.id)
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
        val input = context.openFileInput(localPath)
        val bitmap = BitmapFactory.decodeStream(input, null, imageOptions)
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 50, baos)
        val data = baos.toByteArray()
        storageRef.putBytes(data)

        return remotePath
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
