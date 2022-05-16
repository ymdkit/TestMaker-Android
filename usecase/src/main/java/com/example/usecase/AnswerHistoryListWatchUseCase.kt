package com.example.usecase

import com.example.core.utils.Resource
import com.example.domain.model.DocumentId
import com.example.domain.repository.AnswerHistoryRepository
import com.example.usecase.model.AnswerHistoryUseCaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnswerHistoryListWatchUseCase @Inject constructor(
    private val repository: AnswerHistoryRepository,
) {

    private val _flow: MutableStateFlow<Resource<List<AnswerHistoryUseCaseModel>>> =
        MutableStateFlow(Resource.Empty)
    val flow: StateFlow<Resource<List<AnswerHistoryUseCaseModel>>> = _flow

    private lateinit var workbookId: String

    fun setup(workbookId: String, scope: CoroutineScope) {
        this.workbookId = workbookId
    }

    suspend fun load() {
        _flow.emit(Resource.Loading)

        val groupList = repository.getAnswerHistoryList(workbookId = DocumentId(workbookId))
        _flow.emit(Resource.Success(groupList.map {
            AnswerHistoryUseCaseModel.fromAnswerHistory(it)
        }))
    }
}

