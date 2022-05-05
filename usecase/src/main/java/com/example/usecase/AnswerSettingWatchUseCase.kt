package com.example.usecase

import com.example.domain.repository.PreferenceRepository
import com.example.usecase.model.AnswerSettingUseCaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnswerSettingWatchUseCase @Inject constructor(
    private val repository: PreferenceRepository
) {

    private val _flow: MutableStateFlow<AnswerSettingUseCaseModel> =
        MutableStateFlow(AnswerSettingUseCaseModel.fromAnswerSetting(repository.getAnswerSetting()))
    val flow: StateFlow<AnswerSettingUseCaseModel> = _flow

    fun setup(scope: CoroutineScope) {
        scope.launch {
            repository.updateAnswerSettingFlow.onEach {
                _flow.emit(AnswerSettingUseCaseModel.fromAnswerSetting(it))
            }.launchIn(this)
        }
    }

    fun getAnswerSetting() =
        AnswerSettingUseCaseModel.fromAnswerSetting(repository.getAnswerSetting())
}
