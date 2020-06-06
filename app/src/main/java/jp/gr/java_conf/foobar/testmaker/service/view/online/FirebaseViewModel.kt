package jp.gr.java_conf.foobar.testmaker.service.view.online

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import jp.gr.java_conf.foobar.testmaker.service.domain.RealmTest
import jp.gr.java_conf.foobar.testmaker.service.infra.api.SearchService
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTestResult
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.TestMakerRepository
import kotlinx.coroutines.launch

class FirebaseViewModel(private val repository: TestMakerRepository, private val auth: Auth, private val service: SearchService) : ViewModel() {

    fun getTestsQuery() = repository.getTestsQuery()

    suspend fun downloadTest(testId: String): FirebaseTestResult = repository.downloadTest(testId)

    fun convert(test: FirebaseTest) = repository.createObjectFromFirebase(test)

    fun createUser(user: FirebaseUser?) = repository.setUser(user)

    suspend fun uploadTest(test: RealmTest, overview: String) = repository.createTest(test, overview, "")

    fun getAuthUIIntent(): Intent = auth.getAuthUIIntent()

    fun getUser(): FirebaseUser? = auth.getUser()

    val tests = MutableLiveData<List<FirebaseTest>>()

    fun getTests() {
        viewModelScope.launch {
            tests.postValue(service.tests(""))
        }
    }

}