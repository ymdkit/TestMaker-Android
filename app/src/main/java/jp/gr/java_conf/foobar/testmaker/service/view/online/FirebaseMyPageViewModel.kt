package jp.gr.java_conf.foobar.testmaker.service.view.online

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTestResult
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.TestMakerRepository

class FirebaseMyPageViewModel(private val repository: TestMakerRepository,private val auth: Auth) : ViewModel() {

    fun fetchMyTests() = repository.fetchMyTests()

    fun getMyTests(): LiveData<List<DocumentSnapshot>> = repository.getMyTests()

    fun deleteTest(id: String) = repository.deleteTest(id)

    suspend fun downloadTest(id: String): FirebaseTestResult = repository.downloadTest(id)

    fun convert(test: FirebaseTest) = repository.createObjectFromFirebase(test)

    fun updateProfile(userName: String, completion: () -> Unit) = repository.updateProfile(userName, completion)

    fun getUser(): FirebaseUser? = auth.getUser()

    fun logOut() = auth.logOut()
}