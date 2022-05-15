package com.example.ui.preference

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.QuestionCondition
import com.example.core.TestMakerColor
import com.example.usecase.*
import com.example.usecase.model.AnswerSettingUseCaseModel
import com.example.usecase.model.UserUseCaseModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PreferenceViewModel @Inject constructor(
    private val answerSettingWatchUseCase: AnswerSettingWatchUseCase,
    private val preferenceCommandUseCase: UserPreferenceCommandUseCase,
    private val userWatchUseCase: UserWatchUseCase,
    private val userAuthCommandUseCase: UserAuthCommandUseCase,
    private val themeColorWatchUseCase: ThemeColorWatchUseCase
) : ViewModel() {

    private val _uiState: MutableStateFlow<PreferenceUiState> =
        MutableStateFlow(
            PreferenceUiState(
                answerSetting = answerSettingWatchUseCase.getAnswerSetting(),
                user = null,
                themeColor = TestMakerColor.BLUE
            )
        )
    val uiState: StateFlow<PreferenceUiState>
        get() = _uiState

    private val _logoutEvent: Channel<Unit> = Channel()
    val logoutEvent: ReceiveChannel<Unit>
        get() = _logoutEvent

    fun setup() {
        answerSettingWatchUseCase.setup(
            scope = viewModelScope
        )
        userWatchUseCase.setup(
            scope = viewModelScope
        )
        themeColorWatchUseCase.setup(
            scope = viewModelScope
        )

        viewModelScope.launch {
            answerSettingWatchUseCase.flow
                .onEach {
                    _uiState.emit(
                        _uiState.value.copy(
                            answerSetting = it
                        )
                    )
                }
                .launchIn(this)
            userWatchUseCase.flow
                .onEach {
                    _uiState.emit(
                        _uiState.value.copy(
                            user = it
                        )
                    )
                }
                .launchIn(this)

            themeColorWatchUseCase.flow
                .onEach {
                    _uiState.emit(
                        _uiState.value.copy(
                            themeColor = it
                        )
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

    fun onIsCaseInsensitiveChanged(value: Boolean) =
        viewModelScope.launch {
            preferenceCommandUseCase.putAnswerSetting(
                _uiState.value.answerSetting.copy(
                    isCaseInsensitive = value
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

    fun onThemeColorChanged(value: TestMakerColor) =
        viewModelScope.launch {
            preferenceCommandUseCase.putThemeColor(value)
        }

    fun onUserCreated() =
        viewModelScope.launch {
            userAuthCommandUseCase.registerUser()
        }

    fun onLogoutButtonClicked() =
        viewModelScope.launch {
            userAuthCommandUseCase.logout()
            _logoutEvent.send(Unit)
        }

    fun onDisplayNameSubmitted(value: String) =
        viewModelScope.launch {
            userAuthCommandUseCase.updateUser(displayName = value)
        }

    fun onAdRemoved() =
        viewModelScope.launch {
            preferenceCommandUseCase.putIsRemovedAd(isRemovedAd = true)
        }
}

data class PreferenceUiState(
    val answerSetting: AnswerSettingUseCaseModel,
    val user: UserUseCaseModel?,
    val themeColor: TestMakerColor
)

sealed class EditTextState {
    object Empty : EditTextState()
    data class Editing(val value: String) : EditTextState()
}
