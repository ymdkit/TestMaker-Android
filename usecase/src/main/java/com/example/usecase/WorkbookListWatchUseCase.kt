package com.example.usecase

import com.example.domain.repository.WorkBookRepository
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

@Singleton
class WorkbookListWatchUseCase @Inject constructor(
    private val repository: WorkBookRepository
) {

    private val _flow: MutableStateFlow<Resource<List<WorkbookUseCaseModel>>> =
        MutableStateFlow(Resource.Empty)
    val flow: StateFlow<Resource<List<WorkbookUseCaseModel>>> = _flow

    fun setup(scope: CoroutineScope) {
        scope.launch {

            repository.createWorkbookFlow
                .onEach {
                    val new = _flow.value.map { list ->
                        list + WorkbookUseCaseModel.fromWorkbook(it)
                    }
                    _flow.emit(new)
                }.launchIn(scope)

            repository.updateWorkbookFlow
                .onEach { workbook ->
                    val new = _flow.value.map { list ->
                        list.map {
                            if (workbook.id.value == it.id) WorkbookUseCaseModel.fromWorkbook(
                                workbook
                            ) else it
                        }
                    }
                    _flow.emit(new)
                }.launchIn(scope)

            repository.deleteWorkbookFlow
                .onEach { workbookId ->
                    val new = _flow.value.map { list ->
                        list.filter { it.id != workbookId.value }
                    }
                    _flow.emit(new)
                }.launchIn(scope)
        }
    }

    suspend fun load() {
        _flow.emit(Resource.Loading)
        val workbookList = repository.getWorkbookList()
        _flow.emit(Resource.Success(workbookList.map {
            WorkbookUseCaseModel.fromWorkbook(it)
        }))
    }
}

