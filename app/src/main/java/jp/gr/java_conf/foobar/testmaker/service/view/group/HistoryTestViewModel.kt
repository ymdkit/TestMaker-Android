package jp.gr.java_conf.foobar.testmaker.service.view.group

import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.HistoryRepository

class HistoryTestViewModel(private val repository: HistoryRepository) : ViewModel() {

    suspend fun getHistories(documentId: String) = repository.getHistories(documentId)

}