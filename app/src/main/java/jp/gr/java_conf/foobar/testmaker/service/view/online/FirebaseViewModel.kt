package jp.gr.java_conf.foobar.testmaker.service.view.online

import androidx.lifecycle.ViewModel
import com.example.infra.remote.SearchApi
import com.example.infra.remote.SearchClient
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FirebaseViewModel @Inject constructor(
    @SearchClient private val service: SearchApi
) : ViewModel() {

    // todo ダウンロード数情報を更新する
    suspend fun updateTest(documentId: String, size: Int, downloadCount: Int) = service.updateTest(
        documentId = documentId,
        size = size,
        downloadCount = downloadCount
    )
}