package com.example.usecase

import com.example.core.TestMakerColor
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
class ThemeColorWatchUseCase @Inject constructor(
    private val repository: PreferenceRepository
) {

    private val _flow: MutableStateFlow<TestMakerColor> =
        MutableStateFlow(repository.getThemeColor())
    val flow: StateFlow<TestMakerColor> = _flow

    fun setup(scope: CoroutineScope) {
        scope.launch {
            repository.updateThemeColorFlow.onEach {
                _flow.emit(it)
            }.launchIn(this)
        }
    }
}
