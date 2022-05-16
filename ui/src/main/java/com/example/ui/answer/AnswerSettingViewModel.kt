package com.example.ui.answer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.QuestionCondition
import com.example.usecase.AnswerSettingWatchUseCase
import com.example.usecase.UserPreferenceCommandUseCase
import com.example.usecase.model.AnswerSettingUseCaseModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnswerSettingViewModel @Inject constructor(
    private val answerSettingWatchUseCase: AnswerSettingWatchUseCase,
    private val preferenceCommandUseCase: UserPreferenceCommandUseCase
) : ViewModel() {

    private val _uiState: MutableStateFlow<AnswerSettingUseCaseModel> =
        MutableStateFlow(
            answerSettingWatchUseCase.getAnswerSetting()
        )
    val uiState: StateFlow<AnswerSettingUseCaseModel>
        get() = _uiState

    fun setup() {
        answerSettingWatchUseCase.setup(
            scope = viewModelScope
        )

        viewModelScope.launch {
            answerSettingWatchUseCase.flow
                .onEach {
                    _uiState.value = it
                }
                .launchIn(this)
        }
    }

    fun onIsRandomOrderChanged(value: Boolean) =
        viewModelScope.launch {
            preferenceCommandUseCase.putAnswerSetting(
                _uiState.value.copy(
                    isRandomOrder = value
                )
            )
        }

    fun onIsSwapProblemAndAnswerChanged(value: Boolean) =
        viewModelScope.launch {
            preferenceCommandUseCase.putAnswerSetting(
                _uiState.value.copy(
                    isSwapProblemAndAnswer = value
                )
            )
        }

    fun onQuestionConditionChanged(value: Boolean) =
        viewModelScope.launch {
            preferenceCommandUseCase.putAnswerSetting(
                _uiState.value.copy(
                    questionCondition = if (value) QuestionCondition.WRONG else QuestionCondition.ALL,
                )
            )
        }

    fun onIsSelfScoringChanged(value: Boolean) =
        viewModelScope.launch {
            preferenceCommandUseCase.putAnswerSetting(
                _uiState.value.copy(
                    isSelfScoring = value
                )
            )
        }

    fun onIsAlwaysShowExplanationChanged(value: Boolean) =
        viewModelScope.launch {
            preferenceCommandUseCase.putAnswerSetting(
                _uiState.value.copy(
                    isAlwaysShowExplanation = value
                )
            )
        }

    fun onIsPlaySoundChanged(value: Boolean) =
        viewModelScope.launch {
            preferenceCommandUseCase.putAnswerSetting(
                _uiState.value.copy(
                    isPlaySound = value
                )
            )
        }

    fun onIsShowAnswerSettingDialogChanged(value: Boolean) =
        viewModelScope.launch {
            preferenceCommandUseCase.putAnswerSetting(
                _uiState.value.copy(
                    isShowAnswerSettingDialog = value
                )
            )
        }

    fun onQuestionCountChanged(value: Int) =
        viewModelScope.launch {
            preferenceCommandUseCase.putAnswerSetting(
                _uiState.value.copy(
                    questionCount = value
                )
            )
        }

    fun onStartPositionChanged(value: Int) =
        viewModelScope.launch {
            preferenceCommandUseCase.putAnswerSetting(
                _uiState.value.copy(
                    startPosition = value
                )
            )
        }
}
