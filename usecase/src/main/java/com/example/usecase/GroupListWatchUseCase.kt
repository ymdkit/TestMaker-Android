package com.example.usecase

import com.example.domain.repository.GroupRepository
import com.example.domain.repository.UserRepository
import com.example.usecase.model.GroupUseCaseModel
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
class GroupListWatchUseCase @Inject constructor(
    private val repository: GroupRepository,
    private val userRepository: UserRepository
) {

    private val _flow: MutableStateFlow<Resource<List<GroupUseCaseModel>>> =
        MutableStateFlow(Resource.Empty)
    val flow: StateFlow<Resource<List<GroupUseCaseModel>>> = _flow

    fun setup(scope: CoroutineScope) {
        scope.launch {
            repository.updateGroupListFlow.onEach {
                _flow.emit(Resource.Success(it.map {
                    GroupUseCaseModel.fromGroup(it)
                }))
            }.launchIn(this)
        }
    }

    suspend fun load() {
        _flow.emit(Resource.Loading)
        val user = userRepository.getUserOrNull()

        if (user != null) {
            val groupList = repository.getBelongingGroupList(userId = user.id)
            _flow.emit(Resource.Success(groupList.map {
                GroupUseCaseModel.fromGroup(it)
            }))
        } else {
            _flow.emit(Resource.Failure("does not exist user"))
        }
    }
}

