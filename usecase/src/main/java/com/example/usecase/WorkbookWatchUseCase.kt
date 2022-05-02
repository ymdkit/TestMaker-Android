package com.example.usecase

import com.example.domain.model.WorkbookId
import com.example.domain.repository.WorkBookRepository
import com.example.usecase.model.WorkbookUseCaseModel
import com.example.usecase.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    fun setup(workbookId: Long) {
        this.workbookId = workbookId
    }

    suspend fun load() {
        _flow.emit(Resource.Loading)
        val workbook = repository.getWorkbook(WorkbookId(workbookId))
        _flow.emit(
            Resource.Success(
                WorkbookUseCaseModel.fromWorkbook(workbook)
            )
        )
    }
}

