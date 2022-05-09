package com.example.usecase

import com.example.domain.repository.PreferenceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IsRemovedAdWatchUseCase @Inject constructor(
    private val repository: PreferenceRepository
) {

    private val _flow: MutableStateFlow<Boolean> =
        MutableStateFlow(repository.isRemovedAd())
    val flow: StateFlow<Boolean> = _flow

    fun setup(scope: CoroutineScope) {
        scope.launch {
            repository.updateIsRemovedAdFlow.onEach {
                _flow.emit(it)
            }.launchIn(this)
        }
    }

    suspend fun load() {
        _flow.emit(repository.isRemovedAd())
    }

    fun get() = repository.isRemovedAd()
}

