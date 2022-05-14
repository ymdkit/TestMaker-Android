package com.example.usecase

import com.example.domain.model.WorkbookId
import com.example.domain.repository.WorkBookRepository
import com.example.usecase.model.QuestionUseCaseModel
import com.example.usecase.model.WorkbookUseCaseModel
import com.example.usecase.utils.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.properties.Delegates

@Singleton
class WorkbookWatchUseCase @Inject constructor(
    private val repository: WorkBookRepository
) {

    private val _flow: MutableStateFlow<Resource<WorkbookUseCaseModel>> =
        MutableStateFlow(Resource.Empty)
    val flow: StateFlow<Resource<WorkbookUseCaseModel>> = _flow

    private var workbookId by Delegates.notNull<Long>()

    fun setup(workbookId: Long, scope: CoroutineScope) {
        this.workbookId = workbookId

        scope.launch {
            repository.updateWorkBookListFlow
                .onEach { list ->
                    val newWorkbook = list.find { it.id.value == workbookId } ?: return@onEach
                    _flow.emit(_flow.value.map { WorkbookUseCaseModel.fromWorkbook(newWorkbook) })
                }
                .launchIn(this)
        }
    }

    suspend fun load(
        questionFilter: (QuestionUseCaseModel) -> Boolean = { true }
    ) {
        _flow.emit(Resource.Loading)
        val workbook =
            WorkbookUseCaseModel.fromWorkbook(repository.getWorkbook(WorkbookId(workbookId)))
        _flow.emit(
            Resource.Success(
                workbook.copy(
                    questionList = workbook.questionList.filter(questionFilter)
                )
            )
        )
    }
}

