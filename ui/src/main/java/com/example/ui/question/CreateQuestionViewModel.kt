package com.example.ui.question

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.QuestionImage
import com.example.core.QuestionType
import com.example.usecase.UserQuestionCommandUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates

@HiltViewModel
class CreateQuestionViewModel @Inject constructor(
    private val userQuestionCommandUseCase: UserQuestionCommandUseCase,
) : FormQuestionViewModel, ViewModel() {

    private val _uiState: MutableStateFlow<FormQuestionUiState> =
        MutableStateFlow(
            FormQuestionUiState(
                questionType = QuestionType.WRITE,
                problem = "",
                answerList = listOf(""),
                explanation = "",
                problemImage = QuestionImage.Empty,
                explanationImage = QuestionImage.Empty,
                otherSelectionList = listOf("", "", ""),
                isAutoGenerateOtherSelections = false,
                isCheckAnswerOrder = false
            )
        )
    override val uiState: StateFlow<FormQuestionUiState>
        get() = _uiState

    private val _onCreateQuestion: Channel<Unit> = Channel()
    val onCreateQuestion: ReceiveChannel<Unit>
        get() = _onCreateQuestion

    private var workbookId by Delegates.notNull<Long>()

    fun setup(workbookId: Long) {
        this.workbookId = workbookId
    }

    override fun onQuestionTypeChanged(value: QuestionType) =
        viewModelScope.launch {
            when (value) {
                QuestionType.WRITE ->
                    _uiState.value = _uiState.value.copy(
                        questionType = value,
                        answerList = listOf(_uiState.value.answerList.firstOrNull() ?: ""),
                    )
                QuestionType.SELECT ->
                    _uiState.value = _uiState.value.copy(
                        questionType = value,
                        answerList = listOf(_uiState.value.answerList.firstOrNull() ?: ""),
                    )
                QuestionType.COMPLETE ->
                    _uiState.value = _uiState.value.copy(
                        questionType = value,
                    )
                QuestionType.SELECT_COMPLETE ->
                    _uiState.value = _uiState.value.copy(
                        questionType = value,
                    )
            }

        }

    override fun onProblemChanged(value: String) =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                problem = value
            )
        }

    override fun onAnswerListChanged(value: List<String>) =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                answerList = value
            )
        }

    override fun onOtherSelectionListChanged(value: List<String>) =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                otherSelectionList = value
            )
        }

    override fun onExplanationChanged(value: String) =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                explanation = value
            )
        }

    override fun onProblemImageUrlChanged(value: QuestionImage) =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                problemImage = value
            )
        }

    override fun onExplanationImageUrlChanged(value: QuestionImage) =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                explanationImage = value
            )
        }

    override fun onIsAutoGenerateOtherSelectionsChanged(value: Boolean) =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAutoGenerateOtherSelections = value
            )
        }

    override fun onIsCheckAnswerOrderChanged(value: Boolean) =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCheckAnswerOrder = value
            )
        }

    fun onCreateButtonClicked() =
        viewModelScope.launch {

            val inputValues = _uiState.value

            userQuestionCommandUseCase.createQuestion(
                workbookId = workbookId,
                type = inputValues.questionType.value,
                problem = inputValues.problem,
                answers = inputValues.answerList,
                explanation = inputValues.explanation,
                problemImageUrl = inputValues.problemImage.getRawString(),
                explanationImageUrl = "",
                otherSelections = inputValues.otherSelectionList,
                isAutoGenerateOtherSelections = inputValues.isAutoGenerateOtherSelections,
                isCheckAnswerOrder = inputValues.isCheckAnswerOrder
            )

            _onCreateQuestion.send(Unit)
            resetInputValues()
        }


    private fun resetInputValues() {
        _uiState.value = _uiState.value.copy(
            problem = "",
            answerList = _uiState.value.answerList.map { "" },
            explanation = "",
            problemImage = QuestionImage.Empty,
            explanationImage = QuestionImage.Empty,
            otherSelectionList = _uiState.value.otherSelectionList.map { "" },
        )
    }
}
