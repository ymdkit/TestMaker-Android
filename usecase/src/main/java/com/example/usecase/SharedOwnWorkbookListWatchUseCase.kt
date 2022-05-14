package com.example.usecase

import com.example.core.utils.Resource
import com.example.domain.repository.SharedWorkbookRepository
import com.example.domain.repository.UserRepository
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
class SharedOwnWorkbookListWatchUseCase @Inject constructor(
    private val repository: SharedWorkbookRepository,
    private val userRepository: UserRepository,
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

    suspend fun load() {
        _flow.emit(Resource.Loading)
        val user = userRepository.getUserOrNull()

        if (user != null) {
            val workbookList = repository.getWorkbookListByUserId(userId = user.id)
            _flow.emit(Resource.Success(workbookList.map {
                SharedWorkbookUseCaseModel.fromSharedWorkbook(it)
            }))
        } else {
            _flow.emit(Resource.Failure("does not exist user"))
        }
    }
}

