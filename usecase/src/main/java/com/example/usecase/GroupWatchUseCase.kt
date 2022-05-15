package com.example.usecase

import com.example.core.utils.Resource
import com.example.domain.model.Group
import com.example.domain.model.GroupId
import com.example.domain.repository.GroupRepository
import com.example.usecase.model.GroupUseCaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupWatchUseCase @Inject constructor(
    private val repository: GroupRepository
) {

    private val _flow: MutableStateFlow<Resource<GroupUseCaseModel?>> =
        MutableStateFlow(Resource.Empty)
    val flow: StateFlow<Resource<GroupUseCaseModel?>> = _flow

    private lateinit var groupId: String

    fun setup(groupId: String, scope: CoroutineScope) {
        this.groupId = groupId
        scope.launch {

            repository.updateGroupListFlow.onEach {
                emitGroupOrNull(it.firstOrNull { it.id.value == groupId })
            }.launchIn(this)
        }
    }

    suspend fun load() {
        _flow.emit(Resource.Loading)
        val group = repository.getGroupOrNull(groupId = GroupId(groupId))
        emitGroupOrNull(group)
    }

    private suspend fun emitGroupOrNull(group: Group?) {
        if (group != null) {
            _flow.emit(Resource.Success(GroupUseCaseModel.fromGroup(group)))
        } else {
            _flow.emit(Resource.Success(null))
        }
    }
}

