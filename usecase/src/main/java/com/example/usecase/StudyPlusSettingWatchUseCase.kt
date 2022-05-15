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
class StudyPlusSettingWatchUseCase @Inject constructor(
    private val repository: PreferenceRepository
) {

    private val _flow: MutableStateFlow<String> =
        MutableStateFlow(repository.getStudyPlusSetting())
    val flow: StateFlow<String> = _flow

    fun setup(scope: CoroutineScope) {
        scope.launch {
            repository.updateStudyPlusSettingFlow.onEach {
                _flow.emit(it)
            }.launchIn(this)
        }
    }

    fun getStudyPlusSetting() =
        repository.getStudyPlusSetting()
}
