package jp.gr.java_conf.foobar.testmaker.service.infra.firebase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.*


class RemoteDataSource(val context: Context) {

    private var myTests: MutableLiveData<List<DocumentSnapshot>>? = null

    private val db = FirebaseFirestore.getInstance()

    suspend fun downloadTest(testId: String): FirebaseTestResult {

        val test = db.collection("tests").document(testId).get().await().toObject(FirebaseTest::class.java)
                ?: return FirebaseTestResult.Failure("問題集が見つかりません")

        test.questions = downloadQuestions(testId)

        return FirebaseTestResult.Success(test)
    }

    private suspend fun downloadQuestions(testId: String): List<FirebaseQuestion> {
        return db.collection("tests")
                .document(testId)
                .collection("questions")
                .get()
                .await()
                .toObjects(FirebaseQuestion::class.java).sortedBy { q -> q.order }
    }

    fun setUser(user: FirebaseUser?) {

        user ?: return

        val myUser = MyFirebaseUser(name = user.displayName
                ?: "guest", id = user.uid)

        db.collection("users")
                .document(user.uid)
                .set(myUser)

    }

    suspend fun createTest(test: Test, overview: String): String {

        val firebaseTest = test.toFirebaseTest(context)
        val user = FirebaseAuth.getInstance().currentUser ?: return ""

        firebaseTest.userId = user.uid
        firebaseTest.userName = user.displayName ?: "guest"
        firebaseTest.overview = overview
        firebaseTest.size = test.questionsNonNull().size
        firebaseTest.locale = Locale.getDefault().language

        val ref = db.collection("tests").document()

        val firebaseQuestions = test.questionsNonNull()

        ref.set(firebaseTest).await()

        val batch = db.batch()
        firebaseQuestions.forEach {
            batch.set(ref.collection("questions").document(), it.toFirebaseQuestions(user))

            if (it.imagePath.isNotEmpty()) {
                val storage = FirebaseStorage.getInstance()

                val storageRef = storage.reference.child("${user.uid}/${it.imagePath}")

                val baos = ByteArrayOutputStream()
                val imageOptions = BitmapFactory.Options()
                imageOptions.inPreferredConfig = Bitmap.Config.RGB_565
                val input = context.openFileInput(it.imagePath)
                val bitmap = BitmapFactory.decodeStream(input, null, imageOptions)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos)
                val data = baos.toByteArray()

                storageRef.putBytes(data)
            }
        }

        batch.commit().await()

        return ref.id
    }

    fun getMyTests(): LiveData<List<DocumentSnapshot>> {
        if (myTests == null) {
            myTests = MutableLiveData()
            fetchMyTests()
        }
        return myTests as LiveData<List<DocumentSnapshot>>
    }

    fun fetchMyTests() {

        val user = FirebaseAuth.getInstance().currentUser ?: return

        db.collection("tests").whereEqualTo("userId", user.uid).get()
                .addOnSuccessListener { query ->

                    myTests?.postValue(query.documents)

                }
                .addOnFailureListener {
                    Log.d("Debug", "fetch myTests failure: $it")
                }
    }

    fun deleteTest(id: String) {
        db.collection("tests").document(id).delete()
                .addOnSuccessListener {
                    fetchMyTests()
                    Log.d("Debug", "delete success")
                }
                .addOnFailureListener {
                    Log.d("Debug", "delete failure: $it")
                }
    }

    fun updateProfile(userName: String, completion: () -> Unit) {

        val user = FirebaseAuth.getInstance().currentUser ?: return

        val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(userName).build()

        user.updateProfile(profileUpdates).addOnSuccessListener {
            completion()
        }
        setUser(user)

    }

    fun getTestsQuery() = db.collection("tests").orderBy("created_at", Query.Direction.DESCENDING)

}
