package jp.gr.java_conf.foobar.testmaker.service.view.play

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.gr.java_conf.foobar.testmaker.service.domain.AnswerStatus
import jp.gr.java_conf.foobar.testmaker.service.domain.QuestionFormat
import jp.gr.java_conf.foobar.testmaker.service.domain.QuestionModel
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.db.*
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.TestRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AnswerWorkbookViewModel(
    private val testId: Long,
    private val isRetry: Boolean,
    private val preferences: SharedPreferenceManager,
    private val repository: TestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlayUiState>(PlayUiState.Initial)
    val uiState: StateFlow<PlayUiState> = _uiState

    private val _answerEffectState = MutableStateFlow(AnswerEffectState.None)
    val answerEffectState: StateFlow<AnswerEffectState> = _answerEffectState

    private var answeringQuestions: List<QuestionModel> = emptyList()

    private val workbook: Test by lazy { repository.get(testId) }

    private val isSwap: Boolean = preferences.reverse

    init {
        viewModelScope.launch {

            val workbook = repository.get(testId)
            answeringQuestions = workbook.questions.map { it.toQuestionModel() }

            if (isRetry) {
                answeringQuestions = answeringQuestions.filter { it.isAnswering }
            } else {
                answeringQuestions = answeringQuestions.drop(workbook.startPosition)
            }

            resetAnswering()

            if (preferences.refine) {
                answeringQuestions =
                    answeringQuestions.filter { it.answerStatus == AnswerStatus.INCORRECT }
            }

            if (preferences.random) {
                answeringQuestions = answeringQuestions.shuffled()
            }

            answeringQuestions = answeringQuestions.take(workbook.limit)

            if (answeringQuestions.isEmpty()) {
                _uiState.value = PlayUiState.NoQuestionExist
                return@launch
            }

            loadNext(-1)
        }
    }

    private fun resetAnswering() {
        repository.update(
            workbook.copy(
                questions = workbook.questions
                    .map { it.copy(isSolved = false) }
            )
        )
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
            repository.update(answeringQuestion.toQuestion())

            _uiState.value = when (answeringQuestion.format) {
                QuestionFormat.WRITE ->
                    if (preferences.manual)
                        PlayUiState.Manual(
                            index = index,
                            question = answeringQuestion
                        )
                    else
                        PlayUiState.Write(
                            index = index,
                            question = answeringQuestion
                        )
                QuestionFormat.SELECT -> PlayUiState.Select(
                    index = index,
                    question = answeringQuestion,
                    choices = answeringQuestion.getChoices(
                        workbook.randomExtractedAnswers
                    )
                )
                QuestionFormat.COMPLETE ->
                    if (preferences.manual)
                        PlayUiState.Manual(
                            index = index,
                            question = answeringQuestion
                        )
                    else
                        PlayUiState.Complete(
                            index = index,
                            question = answeringQuestion
                        )
                QuestionFormat.SELECT_COMPLETE -> PlayUiState.SelectComplete(
                    index = index,
                    question = answeringQuestion,
                    choices = answeringQuestion.getChoices(
                        workbook.randomExtractedAnswers
                    )
                )
            }
        }
    }

    fun judgeIsCorrect(index: Int, question: QuestionModel, yourAnswer: String) {
        viewModelScope.launch {
            val isCorrect = yourAnswer == question.getAnswer(isSwap = isSwap)
            setupReview(index, question, yourAnswer, isCorrect)
        }
    }

    fun judgeIsCorrect(index: Int, question: QuestionModel, yourAnswers: List<String>) {
        viewModelScope.launch {
            val isCorrect = question.isCorrect(yourAnswers, isSwap = isSwap)

            setupReview(index, question, yourAnswers.filter{ it.isNotEmpty() }.joinToString("\n"), isCorrect)
        }
    }

    private fun setupReview(
        index: Int,
        question: QuestionModel,
        yourAnswer: String,
        isCorrect: Boolean
    ) {
        val judgedQuestion = question.copy(
            answerStatus = if (isCorrect) AnswerStatus.CORRECT else AnswerStatus.INCORRECT
        )
        repository.update(judgedQuestion.toQuestion())

        _answerEffectState.value =
            if (isCorrect) AnswerEffectState.Correct else AnswerEffectState.Incorrect

        if (preferences.alwaysReview || !isCorrect) {
            _uiState.value = PlayUiState.Review(
                index = index,
                question = judgedQuestion,
                yourAnswer = yourAnswer
            )
        } else {
            viewModelScope.launch(Dispatchers.Default) {
                delay(800)
                loadNext(index)
            }
        }
    }

    fun confirm(index: Int, question: QuestionModel) {
        _uiState.value = PlayUiState.ManualReview(
            index = index,
            question = question
        )
    }

    fun selfJudge(index: Int, question: QuestionModel, isCorrect: Boolean) {
        viewModelScope.launch {
            val judgedQuestion = question.copy(
                answerStatus = if (isCorrect) AnswerStatus.CORRECT else AnswerStatus.INCORRECT
            )

            repository.update(judgedQuestion.toQuestion())

            loadNext(index)
        }
    }

}

sealed class PlayUiState {
    object Initial : PlayUiState()
    data class Write(val index: Int, val question: QuestionModel) : PlayUiState()
    data class Select(val index: Int, val question: QuestionModel, val choices: List<String>) :
        PlayUiState()

    data class Complete(val index: Int, val question: QuestionModel) : PlayUiState()
    data class SelectComplete(
        val index: Int,
        val question: QuestionModel,
        val choices: List<String>
    ) : PlayUiState()

    data class Manual(val index: Int, val question: QuestionModel) : PlayUiState()
    data class ManualReview(val index: Int, val question: QuestionModel) : PlayUiState()
    data class Review(val index: Int, val question: QuestionModel, val yourAnswer: String) :
        PlayUiState()

    object NoQuestionExist : PlayUiState()

    object Finish : PlayUiState()
}

enum class AnswerEffectState {
    None,
    Correct,
    Incorrect
}