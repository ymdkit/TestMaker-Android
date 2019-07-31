package jp.gr.java_conf.foobar.testmaker.service.models

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
import java.io.ByteArrayOutputStream
import java.util.*


class RemoteDataSource(val context: Context) {

    private var onlineTests: MutableLiveData<List<DocumentSnapshot>>? = null

    private var myTests: MutableLiveData<List<DocumentSnapshot>>? = null

    private var downloadTest: MutableLiveData<StructTest>? = null

    private val db = FirebaseFirestore.getInstance()

    fun getTests(): LiveData<List<DocumentSnapshot>> {

        if (onlineTests == null) {
            onlineTests = MutableLiveData()
            fetchTests()
        }
        return onlineTests as LiveData<List<DocumentSnapshot>>

    }

    fun fetchTests() {

        val locale = Locale.getDefault()
        val language = locale.language

        db.collection("tests").orderBy("created_at", Query.Direction.DESCENDING).limit(50).get()
                .addOnSuccessListener { query ->

                    onlineTests?.postValue(query.documents.filter { it.toObject(FirebaseTest::class.java)?.locale == language })

                }
                .addOnFailureListener {

                }
    }

    fun downloadQuestions(testId: String) {

        val collectionTest = db.collection("tests")


        collectionTest.document(testId).get().addOnSuccessListener {

            val test = it.toObject(FirebaseTest::class.java)?.toStructTest(context)
                    ?: StructTest("")

            collectionTest.document(testId)
                    .collection("questions")
                    .get()
                    .addOnSuccessListener { query ->

                        query.toObjects(FirebaseQuestion::class.java).sortedBy { it.order }.forEach { question ->
                            test.problems.add(question.toStructQuestion())
                        }


                        downloadTest?.postValue(test)
                    }
                    .addOnFailureListener {

                    }
        }
    }

    fun getDownloadQuestions(): LiveData<StructTest> {

        if (downloadTest == null) {
            downloadTest = MutableLiveData()
        }
        return downloadTest as LiveData<StructTest>

    }

    fun resetDownloadTest() {
        downloadTest = null
    }

    fun setUser(user: FirebaseUser?) {

        user ?: return

        val myUser = MyFirebaseUser(name = user.displayName ?: "guest", id = user.uid)

        db.collection("users")
                .document(user.uid)
                .set(myUser)

    }

    fun createTest(test: Test, overview: String, success: () -> Unit) {

        val firebaseTest = test.toFirebaseTest(context)
        val user = FirebaseAuth.getInstance().currentUser ?: return

        firebaseTest.userId = user.uid
        firebaseTest.userName = user.displayName ?: "guest"
        firebaseTest.overview = overview
        firebaseTest.size = test.getQuestions().size
        firebaseTest.locale = Locale.getDefault().language


        val ref = db.collection("tests").document()

        ref.set(firebaseTest)
                .addOnSuccessListener {

                    val batch = db.batch()

                    val firebaseQuestions = test.getQuestions()

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
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                            val data = baos.toByteArray()

                            storageRef.putBytes(data)
                        }
                    }

                    batch.commit().addOnSuccessListener {
                        success()
                    }.addOnFailureListener {
                        Log.d("Debug", "question upload failed")
                    }

                }.addOnFailureListener {
                    Log.d("Debug", "test upload failed")
                }

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
                    fetchTests()
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

}
