package jp.gr.java_conf.foobar.testmaker.service.view.online

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import jp.gr.java_conf.foobar.testmaker.service.domain.RealmTest
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.TestMakerRepository

class FirebaseMyPageViewModel(private val repository: TestMakerRepository, private val auth: Auth) : ViewModel() {

    val isLogin = MutableLiveData(false)

    fun fetchMyTests() = repository.fetchMyTests()

    fun getMyTests(): LiveData<List<DocumentSnapshot>> = repository.getMyTests()

    suspend fun deleteTest(id: String) = repository.deleteTest(id)

    suspend fun downloadTest(id: String): FirebaseTest = repository.downloadTest(id)

    fun convert(test: FirebaseTest) = repository.createObjectFromFirebase(test)

    fun getUser(): FirebaseUser? = auth.getUser()

    fun getAuthUIIntent(): Intent = auth.getAuthUIIntent()

    fun createUser(user: FirebaseUser?) = repository.setUser(user)
}