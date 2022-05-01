package jp.gr.java_conf.foobar.testmaker.service.view.group

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.HistoryRepository
import javax.inject.Inject

@HiltViewModel
class HistoryTestViewModel @Inject constructor(
    private val repository: HistoryRepository
) : ViewModel() {

    suspend fun getHistories(documentId: String) = repository.getHistories(documentId)

}