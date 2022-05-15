package com.example.ui.question

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.QuestionImage
import com.example.core.QuestionType
import com.example.usecase.UserQuestionCommandUseCase
import com.example.usecase.WorkbookWatchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates

@HiltViewModel
class EditQuestionViewModel @Inject constructor(
    private val userQuestionCommandUseCase: UserQuestionCommandUseCase,
    private val workbookWatchUseCase: WorkbookWatchUseCase,
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

    private val _onUpdateQuestion: Channel<Unit> = Channel()
    val onUpdateQuestion: ReceiveChannel<Unit>
        get() = _onUpdateQuestion

    private var workbookId by Delegates.notNull<Long>()
    private var questionId by Delegates.notNull<Long>()

    fun setup(
        workbookId: Long,
        questionId: Long
    ) {
        this.workbookId = workbookId
        this.questionId = questionId
        workbookWatchUseCase.setup(
            workbookId = workbookId,
            scope = viewModelScope
        )

        viewModelScope.launch {
            workbookWatchUseCase.flow
                .onEach {
                    val question =
                        it.getOrNull()?.questionList?.find { it.id == questionId } ?: return@onEach
                    _uiState.value =
                        FormQuestionUiState.fromQuestionUseCaseModel(question = question)
                }
                .launchIn(this)
        }
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
                else ->
                    _uiState.value = _uiState.value.copy(
                        questionType = value
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

    fun onUpdateButtonClicked() =
        viewModelScope.launch {

            val inputValues = _uiState.value

            userQuestionCommandUseCase.updateQuestionContents(
                questionId = questionId,
                type = inputValues.questionType,
                problem = inputValues.problem,
                answers = inputValues.answerList,
                explanation = inputValues.explanation,
                problemImageUrl = inputValues.problemImage,
                explanationImageUrl = inputValues.explanationImage,
                otherSelections = inputValues.otherSelectionList,
                isAutoGenerateOtherSelections = inputValues.isAutoGenerateOtherSelections,
                isCheckAnswerOrder = inputValues.isCheckAnswerOrder
            )

            _onUpdateQuestion.send(Unit)
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
