package jp.gr.java_conf.foobar.testmaker.service.view.play

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.AnswerStatus
import com.example.core.QuestionCondition
import com.example.core.QuestionType
import com.example.usecase.*
import com.example.usecase.model.QuestionUseCaseModel
import com.example.usecase.model.WorkbookUseCaseModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates

@HiltViewModel
class AnswerWorkbookViewModel @Inject constructor(
    private val userQuestionCommandUseCase: UserQuestionCommandUseCase,
    private val userWorkbookCommandUseCase: UserWorkbookCommandUseCase,
    private val workbookGetUseCase: WorkbookGetUseCase,
    private val answerSettingWatchUseCase: AnswerSettingWatchUseCase,
    private val judgeUseCase: QuestionJudgeUseCase,
    private val getOrGenerateSelectionListUseCase: GetOrGenerateSelectionListUseCase
) : ViewModel() {

    private var workbookId by Delegates.notNull<Long>()

    private val _uiState = MutableStateFlow<PlayUiState>(PlayUiState.Initial)
    val uiState: StateFlow<PlayUiState> = _uiState

    private val _answerEffectState = MutableStateFlow(AnswerEffectState.None)
    val answerEffectState: StateFlow<AnswerEffectState> = _answerEffectState

    private var answeringQuestions: List<QuestionUseCaseModel> = emptyList()

    private lateinit var workbook: WorkbookUseCaseModel

    fun setup(
        workbookId: Long,
        isRetry: Boolean
    ) {
        this.workbookId = workbookId
        answerSettingWatchUseCase.setup(viewModelScope)

        viewModelScope.launch {
            workbook = workbookGetUseCase.getWorkbook(workbookId = workbookId)

            answeringQuestions = workbook.questionList

            if (isRetry) {
                answeringQuestions = answeringQuestions.filter { it.isAnswering }
            } else {
                answeringQuestions = answeringQuestions.drop(
                    answerSettingWatchUseCase.flow.value.startPosition
                )
            }

            userWorkbookCommandUseCase.resetWorkbookIsAnswering(workbookId)

            if (answerSettingWatchUseCase.flow.value.questionCondition == QuestionCondition.WRONG) {
                answeringQuestions =
                    answeringQuestions.filter { it.answerStatus == AnswerStatus.INCORRECT }
            }

            if (answerSettingWatchUseCase.flow.value.isRandomOrder) {
                answeringQuestions = answeringQuestions.shuffled()
            }

            answeringQuestions = answeringQuestions.take(
                answerSettingWatchUseCase.flow.value.questionCount
            )

            if (answeringQuestions.isEmpty()) {
                _uiState.value = PlayUiState.NoQuestionExist
                return@launch
            }

            loadNext(-1)
        }
    }

    fun loadNext(oldIndex: Int) {
        viewModelScope.launch {
            _answerEffectState.value = AnswerEffectState.None

            val index = oldIndex + 1

            if (index >= answeringQuestions.size) {
                _uiState.value = PlayUiState.Finish
                return@launch
            }

            val answeringQuestion = answeringQuestions[index].copy(isAnswering = true)
            userQuestionCommandUseCase.updateQuestion(answeringQuestion)

            _uiState.value = when (answeringQuestion.type) {
                QuestionType.WRITE ->
                    if (answerSettingWatchUseCase.flow.value.isSelfScoring)
                        PlayUiState.Manual(
                            index = index,
                            question = answeringQuestion
                        )
                    else
                        PlayUiState.Write(
                            index = index,
                            question = answeringQuestion
                        )
                QuestionType.SELECT -> PlayUiState.Select(
                    index = index,
                    question = answeringQuestion,
                    choices = getOrGenerateSelectionListUseCase.getOrGenerateSelectionList(
                        workbook = workbook,
                        question = answeringQuestion
                    )
                )
                QuestionType.COMPLETE ->
                    if (answerSettingWatchUseCase.flow.value.isSelfScoring)
                        PlayUiState.Manual(
                            index = index,
                            question = answeringQuestion
                        )
                    else
                        PlayUiState.Complete(
                            index = index,
                            question = answeringQuestion
                        )
                QuestionType.SELECT_COMPLETE -> PlayUiState.SelectComplete(
                    index = index,
                    question = answeringQuestion,
                    choices = getOrGenerateSelectionListUseCase.getOrGenerateSelectionList(
                        workbook = workbook,
                        question = answeringQuestion
                    )
                )
            }
        }
    }

    fun judgeIsCorrect(index: Int, question: QuestionUseCaseModel, yourAnswer: String) {
        viewModelScope.launch {
            val isCorrect = judgeUseCase.judge(
                expect = question,
                actual = listOf(yourAnswer)
            )
            setupReview(index, question, yourAnswer, isCorrect)
        }
    }

    fun judgeIsCorrect(index: Int, question: QuestionUseCaseModel, yourAnswers: List<String>) {
        viewModelScope.launch {
            val isCorrect = judgeUseCase.judge(
                expect = question,
                actual = yourAnswers
            )

            setupReview(
                index,
                question,
                yourAnswers.filter { it.isNotEmpty() }.joinToString("\n"),
                isCorrect
            )
        }
    }

    private fun setupReview(
        index: Int,
        question: QuestionUseCaseModel,
        yourAnswer: String,
        isCorrect: Boolean
    ) {
        viewModelScope.launch {
            val judgedQuestion = question.copy(
                answerStatus = if (isCorrect) AnswerStatus.CORRECT else AnswerStatus.INCORRECT
            )
            userQuestionCommandUseCase.updateQuestion(judgedQuestion)

            _answerEffectState.value =
                if (isCorrect) AnswerEffectState.Correct else AnswerEffectState.Incorrect

            if (answerSettingWatchUseCase.flow.value.isAlwaysShowExplanation || !isCorrect) {
                _uiState.value = PlayUiState.Review(
                    index = index,
                    question = judgedQuestion,
                    yourAnswer = yourAnswer
                )
            } else {
                _uiState.value = PlayUiState.WaitingNextQuestion(index, question)
                delay(800)
                loadNext(index)
            }
        }
    }

    fun confirm(index: Int, question: QuestionUseCaseModel) {
        _uiState.value = PlayUiState.ManualReview(
            index = index,
            question = question
        )
    }

    fun selfJudge(index: Int, question: QuestionUseCaseModel, isCorrect: Boolean) {
        viewModelScope.launch {
            val judgedQuestion = question.copy(
                answerStatus = if (isCorrect) AnswerStatus.CORRECT else AnswerStatus.INCORRECT
            )
            userQuestionCommandUseCase.updateQuestion(judgedQuestion)
            loadNext(index)
        }
    }

}

sealed class PlayUiState {
    object Initial : PlayUiState()
    data class Write(val index: Int, val question: QuestionUseCaseModel) : PlayUiState()
    data class Select(
        val index: Int,
        val question: QuestionUseCaseModel,
        val choices: List<String>
    ) :
        PlayUiState()

    data class Complete(val index: Int, val question: QuestionUseCaseModel) : PlayUiState()
    data class SelectComplete(
        val index: Int,
        val question: QuestionUseCaseModel,
        val choices: List<String>
    ) : PlayUiState()

    data class Manual(val index: Int, val question: QuestionUseCaseModel) : PlayUiState()
    data class ManualReview(val index: Int, val question: QuestionUseCaseModel) : PlayUiState()
    data class Review(val index: Int, val question: QuestionUseCaseModel, val yourAnswer: String) :
        PlayUiState()

    // 正解時に確実に再 Compose するための State
    data class WaitingNextQuestion(val index: Int, val question: QuestionUseCaseModel) :
        PlayUiState()

    object NoQuestionExist : PlayUiState()

    object Finish : PlayUiState()
}

enum class AnswerEffectState {
    None,
    Correct,
    Incorrect
}