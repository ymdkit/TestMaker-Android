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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FirebaseViewModel(private val repository: TestMakerRepository, private val auth: Auth, private val service: SearchService) : ViewModel() {

    suspend fun downloadTest(testId: String): FirebaseTestResult = repository.downloadTest(testId)

    fun convert(test: FirebaseTest) = repository.createObjectFromFirebase(test)

    fun createUser(user: FirebaseUser?) = repository.setUser(user)

    suspend fun uploadTest(test: RealmTest, overview: String, isPublic: Boolean) = repository.createTest(test, overview, "", isPublic)

    fun getAuthUIIntent(): Intent = auth.getAuthUIIntent()

    fun getUser(): FirebaseUser? = auth.getUser()

    val tests = MutableLiveData<List<FirebaseTest>>()
    val loading = MutableLiveData<Boolean>()
    val error = MutableLiveData<Throwable>()

    fun getTests(keyword: String = "") {
        viewModelScope.launch {
            loading.value = true
            runCatching {
                withContext(Dispatchers.IO) {
                    service.tests(keyword)
                }
            }.onSuccess {
                tests.postValue(it)
            }.onFailure {
                error.postValue(it)
            }
            loading.value = false
        }
    }

}