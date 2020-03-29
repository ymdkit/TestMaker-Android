package jp.gr.java_conf.foobar.testmaker.service.view.play

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NewPlayViewModel(private val questions: List<Question>) : ViewModel() {

    val index = MutableLiveData(0)

    val selectedQuestion = MutableLiveData(Question())

    val answer = MutableLiveData("")
    val answers = List(COMPLETE_ANSWER_MAX) { MutableLiveData("") }
    val selections = List(SELECTION_MAX) { MutableLiveData("") }

    val state = MutableLiveData(State.INITIAL)
    val judgeState = MutableLiveData(JudgeState.NONE)

    val yourAnswer = MutableLiveData("")

    fun loadNext() {
        index.value?.let {
            if (it >= questions.size) {
                viewModelScope.launch {
                    state.value = State.FINISH
                    delay(300)
                }
            } else {
                val question = questions[it]
                when (question.type) {
                    Constants.WRITE -> {
                        state.value = State.WRITE
                    }
                    Constants.SELECT -> {
                        state.value = State.SELECT
                    }
                    Constants.COMPLETE -> {
                        state.value = State.COMPLETE
                    }
                    Constants.SELECT_COMPLETE -> {
                        state.value = State.SELECT_COMPLETE
                    }
                    else -> {
                    }
                }

                selectedQuestion.value = question
                index.value = it + 1
                state.value = State.getStateFromType(question)
                // todo 自動生成モードの対応 選択完答に対応
                selections.forEach { it.value = "" }
                (question.others + listOf(question.answer)).shuffled().forEachIndexed { index, it ->
                    selections[index].value = it
                }
            }
        }
    }

    fun judge(yourAnswer: String) {
        this.yourAnswer.value = yourAnswer

        selectedQuestion.value?.let { question ->

            if (question.isCorrect(yourAnswer, isReverse = false, isCaseInsensitive = false)) {
                loadNext()
            } else {

            }
        }
    }

    fun judge() {

        var isCorrect = false

        selectedQuestion.value?.let { question ->
            when (question.type) {
                Constants.WRITE -> {
                    answer.value?.let {
                        if (question.isCorrect(it, isReverse = false, isCaseInsensitive = false)) {
                            isCorrect = true
                        }
                        yourAnswer.value = it
                    }
                }
                Constants.COMPLETE -> {
                    yourAnswer.value = answers.take(selectedQuestion.value?.answers?.size
                            ?: 0).map { it.value ?: "" }.joinToString(separator = "\n")

                }
                Constants.SELECT_COMPLETE -> {
                    answer.value?.let {
                    }
                }
                else -> {
                }
            }
        }

        if (isCorrect) {
            //todo: activityでやった方が良さそう
            viewModelScope.launch {
                judgeState.value = if (isCorrect) JudgeState.CORRECT else JudgeState.INCORRECT
                delay(1000)
                loadNext()
                judgeState.value = JudgeState.NONE
            }
        } else {
            state.value = State.REVIEW
        }

    }

    companion object {
        private const val COMPLETE_ANSWER_MAX = 4
        private const val SELECTION_MAX = 6
    }

}

enum class State {
    INITIAL,
    WRITE,
    SELECT,
    COMPLETE,
    SELECT_COMPLETE,
    REVIEW,
    FINISH;

    companion object {
        fun getStateFromType(question: Question): State =
                when (question.type) {
                    Constants.WRITE -> WRITE
                    Constants.SELECT -> SELECT
                    Constants.COMPLETE -> COMPLETE
                    Constants.SELECT_COMPLETE -> SELECT_COMPLETE
                    else -> INITIAL
                }

    }
}

enum class JudgeState {
    NONE,
    CORRECT,
    INCORRECT
}