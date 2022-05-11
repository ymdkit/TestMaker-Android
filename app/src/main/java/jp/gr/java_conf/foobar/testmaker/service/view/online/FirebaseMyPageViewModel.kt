package jp.gr.java_conf.foobar.testmaker.service.view.online

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.infra.remote.entity.FirebaseTest
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.gr.java_conf.foobar.testmaker.service.domain.CreateTestSource
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.TestMakerRepository
import javax.inject.Inject

@HiltViewModel
class FirebaseMyPageViewModel @Inject constructor(
    private val repository: TestMakerRepository,
    private val auth: Auth
) :
    ViewModel() {

    val isLogin = MutableLiveData(false)

    fun fetchMyTests() = repository.fetchMyTests()

    fun getMyTests(): LiveData<List<DocumentSnapshot>> = repository.getMyTests()

    suspend fun deleteTest(id: String) = repository.deleteTest(id)

    suspend fun downloadTest(id: String): FirebaseTest = repository.downloadTest(id)

    fun convert(test: FirebaseTest) =
        repository.createObjectFromFirebase(
            firebaseTest = test,
            source = CreateTestSource.SELF_DOWNLOAD.title
        )

    fun getUser(): FirebaseUser? = auth.getUser()

    fun createUser(user: FirebaseUser?) = repository.setUser(user)
}