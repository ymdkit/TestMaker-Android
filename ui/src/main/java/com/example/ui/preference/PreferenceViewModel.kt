package com.example.ui.preference

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
class PreferenceViewModel @Inject constructor(
    private val answerSettingWatchUseCase: AnswerSettingWatchUseCase,
    private val preferenceCommandUseCase: UserPreferenceCommandUseCase
) : ViewModel() {

    private val _uiState: MutableStateFlow<PreferenceUiState> =
        MutableStateFlow(
            PreferenceUiState(
                answerSetting = answerSettingWatchUseCase.getAnswerSetting(),
            )
        )
    val uiState: StateFlow<PreferenceUiState>
        get() = _uiState

    fun setup() {
        answerSettingWatchUseCase.setup(
            scope = viewModelScope
        )

        viewModelScope.launch {
            answerSettingWatchUseCase.flow
                .onEach {
                    _uiState.value = PreferenceUiState(
                        answerSetting = it,
                    )
                }
                .launchIn(this)
        }
    }

    fun onIsRandomOrderChanged(value: Boolean) =
        viewModelScope.launch {
            preferenceCommandUseCase.putAnswerSetting(
                _uiState.value.answerSetting.copy(
                    isRandomOrder = value
                )
            )
        }

    fun onIsSwapProblemAndAnswerChanged(value: Boolean) =
        viewModelScope.launch {
            preferenceCommandUseCase.putAnswerSetting(
                _uiState.value.answerSetting.copy(
                    isSwapProblemAndAnswer = value
                )
            )
        }

    fun onQuestionConditionChanged(value: Boolean) =
        viewModelScope.launch {
            preferenceCommandUseCase.putAnswerSetting(
                _uiState.value.answerSetting.copy(
                    questionCondition = if (value) QuestionCondition.WRONG else QuestionCondition.ALL,
                )
            )
        }

    fun onIsSelfScoringChanged(value: Boolean) =
        viewModelScope.launch {
            preferenceCommandUseCase.putAnswerSetting(
                _uiState.value.answerSetting.copy(
                    isSelfScoring = value
                )
            )
        }

    fun onIsAlwaysShowExplanationChanged(value: Boolean) =
        viewModelScope.launch {
            preferenceCommandUseCase.putAnswerSetting(
                _uiState.value.answerSetting.copy(
                    isAlwaysShowExplanation = value
                )
            )
        }

    fun onIsPlaySoundChanged(value: Boolean) =
        viewModelScope.launch {
            preferenceCommandUseCase.putAnswerSetting(
                _uiState.value.answerSetting.copy(
                    isPlaySound = value
                )
            )
        }

    fun onIsShowAnswerSettingDialogChanged(value: Boolean) =
        viewModelScope.launch {
            preferenceCommandUseCase.putAnswerSetting(
                _uiState.value.answerSetting.copy(
                    isShowAnswerSettingDialog = value
                )
            )
        }

    fun onQuestionCountChanged(value: Int) =
        viewModelScope.launch {
            preferenceCommandUseCase.putAnswerSetting(
                _uiState.value.answerSetting.copy(
                    questionCount = value
                )
            )
        }

    fun onStartPositionChanged(value: Int) =
        viewModelScope.launch {
            preferenceCommandUseCase.putAnswerSetting(
                _uiState.value.answerSetting.copy(
                    startPosition = value
                )
            )
        }

    fun onLoginButtonClicked() =
        viewModelScope.launch {

        }

    fun onLogoutButtonClicked() =
        viewModelScope.launch {

        }

    fun onUserNameChanged(value: String) =
        viewModelScope.launch {

        }

    fun onRemoveAdButtonClicked() =
        viewModelScope.launch {

        }
}

data class PreferenceUiState(
    val answerSetting: AnswerSettingUseCaseModel,
)
