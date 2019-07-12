package jp.gr.java_conf.foobar.testmaker.service.models

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class RemoteDataSource(val context: Context) {

    private var onlineTests: MutableLiveData<List<DocumentSnapshot>>? = null

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

        db.collection("tests").get()
                .addOnSuccessListener { query ->

                    onlineTests?.postValue(query.documents)

                }
                .addOnFailureListener {

                }
    }

    fun downloadQuestions(testId: String) {

        val collectionTest = db.collection("tests")


        collectionTest.document(testId).get().addOnSuccessListener {

            val test = it.toObject(FirebaseTest::class.java)?.toStructTest(context) ?: StructTest("")

            collectionTest.document(testId)
                    .collection("questions")
                    .get()
                    .addOnSuccessListener {

                        it.toObjects(FirebaseQuestion::class.java).forEach {question ->
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

}
