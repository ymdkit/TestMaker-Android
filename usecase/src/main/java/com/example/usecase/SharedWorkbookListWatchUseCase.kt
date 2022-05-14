package com.example.usecase

import com.example.core.utils.Resource
import com.example.domain.repository.SharedWorkbookRepository
import com.example.usecase.model.SharedWorkbookUseCaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedWorkbookListWatchUseCase @Inject constructor(
    private val repository: SharedWorkbookRepository,
) {

    private val _flow: MutableStateFlow<Resource<List<SharedWorkbookUseCaseModel>>> =
        MutableStateFlow(Resource.Empty)
    val flow: StateFlow<Resource<List<SharedWorkbookUseCaseModel>>> = _flow

    fun setup(scope: CoroutineScope) {
        scope.launch {
            repository.updateWorkbookListFlow.onEach {
                _flow.emit(Resource.Success(it.map {
                    SharedWorkbookUseCaseModel.fromSharedWorkbook(it)
                }))
            }.launchIn(this)
        }
    }

    suspend fun load(query: String) {
        _flow.emit(Resource.Loading)

        val workbookList = repository.getWorkbookList(query = query)
        _flow.emit(Resource.Success(workbookList.map {
            SharedWorkbookUseCaseModel.fromSharedWorkbook(it)
        }))
    }
}

