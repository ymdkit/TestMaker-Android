package jp.gr.java_conf.foobar.testmaker.service.view.result

import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.domain.History
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.HistoryRepository

class ResultViewModel(private val repository: HistoryRepository) : ViewModel() {

    suspend fun createHistory(documentId: String, history: History) = repository.createHistory(documentId, history)

}