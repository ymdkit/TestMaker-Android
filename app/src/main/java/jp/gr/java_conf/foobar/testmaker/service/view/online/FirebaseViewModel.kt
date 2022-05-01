package jp.gr.java_conf.foobar.testmaker.service.view.online

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.gr.java_conf.foobar.testmaker.service.domain.CreateTestSource
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.api.SearchService
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.TestMakerRepository
import jp.gr.java_conf.foobar.testmaker.service.modules.SearchClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class FirebaseViewModel @Inject constructor(
    private val repository: TestMakerRepository,
    private val auth: Auth,
    @SearchClient private val service: SearchService
) : ViewModel() {

    suspend fun downloadTest(testId: String): FirebaseTest = repository.downloadTest(testId)

    suspend fun updateTest(documentId: String, size: Int, downloadCount: Int) = service.updateTest(
        documentId = documentId,
        size = size,
        downloadCount = downloadCount
    )

    fun convert(test: FirebaseTest) =
        repository.createObjectFromFirebase(
            firebaseTest = test,
            source = CreateTestSource.PUBLIC_DOWNLOAD.title
        )

    suspend fun uploadTestInGroup(test: Test, overview: String, groupId: String) =
        repository.createTestInGroup(test, overview, groupId)

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