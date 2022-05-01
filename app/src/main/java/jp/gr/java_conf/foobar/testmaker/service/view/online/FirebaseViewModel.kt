package jp.gr.java_conf.foobar.testmaker.service.view.online

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import jp.gr.java_conf.foobar.testmaker.service.domain.CreateTestSource
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.api.SearchService
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.RemoteDataSource
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.TestMakerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FirebaseViewModel(
    private val repository: TestMakerRepository,
    private val auth: Auth,
    private val service: SearchService
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

    suspend fun createTest(test: Test, overview: String, isPublic: Boolean) =
        repository.createTest(test, overview, isPublic)

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

    suspend fun isAlreadyUploaded(title: String): FirebaseTest? {
        val userId = getUser()?.uid ?: return null
        return repository.getTestsByUserId(userId).firstOrNull { it.name == title }
    }

    suspend fun overwriteTest(
        documentId: String,
        test: Test,
        overview: String,
        isPublic: Boolean = true
    ): RemoteDataSource.FirebasePostResponse {
        repository.deleteTest(documentId)
        return repository.createTest(test, overview, isPublic)
    }

}