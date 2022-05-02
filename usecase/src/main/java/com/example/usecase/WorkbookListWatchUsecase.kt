package com.example.usecase

import com.example.domain.model.Workbook
import com.example.domain.repository.WorkBookRepository
import com.example.usecase.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkbookListWatchUsecase @Inject constructor(
    private val repository: WorkBookRepository
) {

    private val _flow: MutableStateFlow<Resource<List<Workbook>>> = MutableStateFlow(Resource.Empty)
    val flow: StateFlow<Resource<List<Workbook>>> = _flow

    suspend fun load() {
        _flow.emit(Resource.Loading)
        val workbookList = repository.getWorkbookList()
        _flow.emit(Resource.Success(workbookList))
    }

}

