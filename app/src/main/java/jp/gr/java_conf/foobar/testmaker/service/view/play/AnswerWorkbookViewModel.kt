package jp.gr.java_conf.foobar.testmaker.service.view.play

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.gr.java_conf.foobar.testmaker.service.domain.AnswerStatus
import jp.gr.java_conf.foobar.testmaker.service.domain.QuestionFormat
import jp.gr.java_conf.foobar.testmaker.service.domain.QuestionModel
import jp.gr.java_conf.foobar.testmaker.service.infra.db.*
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.TestRepository
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

    private var answeringQuestions: List<QuestionModel> = emptyList()

    init {
        viewModelScope.launch {
            resetAnswering()

            val workbook = repository.get(testId)
            answeringQuestions = workbook.questions.map { it.toQuestionModel() }

            if (isRetry) {
                answeringQuestions = answeringQuestions.filter { it.isAnswering }
            } else {
                answeringQuestions = answeringQuestions.drop(workbook.startPosition)
            }

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

            loadNext()
        }
    }

    private suspend fun resetAnswering() {
        // todo
        //val questions = db.workBookDao().findById(testId)!!.toWorkBookModel().questions

//        db.questionStateDao().insertAll(
//            *questions.map {
//                QuestionState(
//                    questionId = it.id,
//                    isAnswering = false,
//                    answerStatus = it.answerStatus
//                )
//            }.toTypedArray()
//        )
    }

    fun loadNext() {
        viewModelScope.launch {

            val index = uiState.value.getNewIndex()

            if (index >= answeringQuestions.size) {
                _uiState.value = PlayUiState.Finish
                return@launch
            }

            val answeringQuestion = answeringQuestions[index].copy(isAnswering = true)

            // todo
//
//            db.questionStateDao().insert(
//                QuestionState(
//                    questionId = answeringQuestion.id,
//                    isAnswering = true,
//                    answerStatus = answeringQuestion.answerStatus
//                )
//            )

            _uiState.value = when (answeringQuestion.format) {
                QuestionFormat.WRITE -> PlayUiState.Write(
                    index = index,
                    question = answeringQuestion
                )
                QuestionFormat.SELECT -> PlayUiState.Select(
                    index = index,
                    question = answeringQuestion
                )
                QuestionFormat.COMPLETE -> PlayUiState.Complete(
                    index = index,
                    question = answeringQuestion
                )
                QuestionFormat.SELECT_COMPLETE -> PlayUiState.SelectComplete(
                    index = index,
                    question = answeringQuestion
                )
            }
        }
    }

    // todo 正解時にスキップする機能の追加
    fun judgeIsCorrect(index: Int, question: QuestionModel, yourAnswer: String) {
        viewModelScope.launch {
            val isCorrect = question.isCorrect(yourAnswer)


            val judgedQuestion = question.copy(
                answerStatus = if (isCorrect) AnswerStatus.CORRECT else AnswerStatus.INCORRECT
            )

            // todo 正誤判定の保存
//            db.questionStateDao().insert(
//                QuestionState(
//                    questionId = judgedQuestion.id,
//                    isAnswering = judgedQuestion.isAnswering,
//                    answerStatus = judgedQuestion.answerStatus
//                )
//            )


            _uiState.value = PlayUiState.Review(
                index = index,
                question = judgedQuestion,
                yourAnswer = yourAnswer
            )
        }
    }

    fun judgeIsCorrect(index: Int, question: QuestionModel, yourAnswers: List<String>) {
        viewModelScope.launch {
            val isCorrect = question.isCorrect(yourAnswers)
            val judgedQuestion = question.copy(
                answerStatus = if (isCorrect) AnswerStatus.CORRECT else AnswerStatus.INCORRECT
            )
// todo

//            db.questionStateDao().insert(
//                QuestionState(
//                    questionId = judgedQuestion.id,
//                    isAnswering = judgedQuestion.isAnswering,
//                    answerStatus = judgedQuestion.answerStatus
//                )
//            )

            _uiState.value = PlayUiState.Review(
                index = index,
                question = judgedQuestion,
                yourAnswer = yourAnswers.joinToString("\n")
            )
        }
    }

    fun confirm(index: Int, question: QuestionModel) {
        _uiState.value = PlayUiState.ManualReview(
            index = index,
            question = question
        )
    }

    fun selfJudge(question: QuestionModel, isCorrect: Boolean) {
        viewModelScope.launch {
            val judgedQuestion = question.copy(
                answerStatus = if (isCorrect) AnswerStatus.CORRECT else AnswerStatus.INCORRECT
            )

            // todo 解答状況の保存

//            db.questionStateDao().insert(
//                QuestionState(
//                    questionId = judgedQuestion.id,
//                    isAnswering = judgedQuestion.isAnswering,
//                    answerStatus = judgedQuestion.answerStatus
//                )
//            )

            loadNext()
        }
    }

}

sealed class PlayUiState {
    object Initial : PlayUiState()
    data class Write(val index: Int, val question: QuestionModel) : PlayUiState()
    data class Select(val index: Int, val question: QuestionModel) : PlayUiState()
    data class Complete(val index: Int, val question: QuestionModel) : PlayUiState()
    data class SelectComplete(val index: Int, val question: QuestionModel) : PlayUiState()
    data class Manual(val index: Int, val question: QuestionModel) : PlayUiState()
    data class ManualReview(val index: Int, val question: QuestionModel) : PlayUiState()
    data class Review(val index: Int, val question: QuestionModel, val yourAnswer: String) :
        PlayUiState()

    object NoQuestionExist : PlayUiState()

    object Finish : PlayUiState()

    fun getNewIndex(): Int =
        when (this) {
            is Initial -> 0
            is ManualReview -> index + 1
            is Review -> index + 1
            else -> 0
        }
}