package com.example.usecase

import com.example.domain.model.UserId
import com.example.domain.repository.SharedWorkbookRepository
import com.example.usecase.model.SharedWorkbookUseCaseModel
import com.example.usecase.utils.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedWorkbookListWatchUseCase @Inject constructor(
    private val repository: SharedWorkbookRepository
) {

    private val _flow: MutableStateFlow<Resource<List<SharedWorkbookUseCaseModel>>> =
        MutableStateFlow(Resource.Empty)
    val flow: StateFlow<Resource<List<SharedWorkbookUseCaseModel>>> = _flow

    private lateinit var userId: String

    fun setup(userId: String, scope: CoroutineScope) {
        this.userId = userId
    }

    suspend fun load() {
        _flow.emit(Resource.Loading)
        val workbookList = repository.getWorkbookListByUserId(userId = UserId(userId))
        _flow.emit(Resource.Success(workbookList.map {
            SharedWorkbookUseCaseModel.fromSharedWorkbook(it)
        }))
    }
}

