package jp.gr.java_conf.foobar.testmaker.service.models

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.UserProfileChangeRequest


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

        db.collection("tests").limit(50).get()
                .addOnSuccessListener { query ->

                    onlineTests?.postValue(query.documents)

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
                    .addOnSuccessListener {

                        it.toObjects(FirebaseQuestion::class.java).forEach { question ->
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

        db.collection("tests")
                .document()
                .set(firebaseTest)
                .addOnSuccessListener {
                    success()
                }.addOnFailureListener {

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
