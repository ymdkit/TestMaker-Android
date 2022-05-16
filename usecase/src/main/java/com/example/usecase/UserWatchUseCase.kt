package com.example.usecase

import com.example.domain.model.User
import com.example.domain.repository.UserRepository
import com.example.usecase.model.UserUseCaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserWatchUseCase @Inject constructor(
    private val repository: UserRepository
) {

    private val _flow: MutableStateFlow<UserUseCaseModel?> =
        MutableStateFlow(null)
    val flow: StateFlow<UserUseCaseModel?> = _flow

    fun setup(scope: CoroutineScope) {
        scope.launch {
            val user = repository.getUserOrNull()
            emitUserOrNull(user)

            repository.updateUserFlow.onEach {
                emitUserOrNull(it)
            }.launchIn(this)
        }
    }

    private suspend fun emitUserOrNull(user: User?) {
        if (user != null) {
            _flow.emit(UserUseCaseModel.fromUser(user))
        } else {
            _flow.emit(null)
        }
    }
}

