package jp.gr.java_conf.foobar.testmaker.service.view.online

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.infra.remote.SearchApi
import com.example.infra.remote.SearchClient
import com.example.infra.remote.entity.FirebaseTest
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.TestMakerRepository
import javax.inject.Inject

@HiltViewModel
class FirebaseViewModel @Inject constructor(
    private val repository: TestMakerRepository,
    @SearchClient private val service: SearchApi
) : ViewModel() {

    suspend fun updateTest(documentId: String, size: Int, downloadCount: Int) = service.updateTest(
        documentId = documentId,
        size = size,
        downloadCount = downloadCount
    )

    suspend fun uploadTestInGroup(test: Test, overview: String, groupId: String) =
        repository.createTestInGroup(test, overview, groupId)

    val tests = MutableLiveData<List<FirebaseTest>>()
}