package jp.gr.java_conf.foobar.testmaker.service.view.play

import android.view.View
import android.widget.Button
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NewPlayViewModel(private val questions: List<Question>, private val preferences: SharedPreferenceManager) : ViewModel() {

    val index = MutableLiveData(0)
    val selectedQuestion = MutableLiveData(Question())

    val answer = MutableLiveData("")
    val answers = List(COMPLETE_ANSWER_MAX) { MutableLiveData("") }
    val selections = List(SELECTION_MAX) { MutableLiveData("") }
    val checkLists = List(SELECTION_MAX) { MutableLiveData(false) }

    val state = MutableLiveData(State.INITIAL)
    val judgeState = MutableLiveData(JudgeState.NONE)

    val yourAnswer = MutableLiveData("")

    val isReversible = preferences.reverse
    private val isCaseInsensitive = preferences.isCaseInsensitive

    fun loadNext() {
        index.value?.let {
            if (it >= questions.size) {
                state.value = State.FINISH
            } else {
                val question = questions[it]

                judgeState.value = JudgeState.NONE
                selectedQuestion.value = question
                checkLists.forEach {
                    it.value = false
                }
                answer.value = ""
                index.value = it + 1
                state.value = State.getStateFromType(question)
                if (isReversible) state.value = State.WRITE
                // todo 自動生成モードの対応 選択完答に対応
                selections.forEach { it.value = "" }
                (question.others + listOf(question.answer)).shuffled().forEachIndexed { index, it ->
                    selections[index].value = it
                }
            }
        }
    }

    fun judge(view: View) {
        (view as Button).text.toString().let {
            yourAnswer.value = it
            selectedQuestion.value?.let { question ->
                judgeResult(question.isCorrect(it, isReverse = isReversible, isCaseInsensitive = isCaseInsensitive))
            }
        }
    }

    fun judge() {

        var isCorrect = false
        selectedQuestion.value?.let { question ->

            when (question.type) {
                Constants.WRITE -> {
                    answer.value?.let {
                        isCorrect = question.isCorrect(it, isReversible, isCaseInsensitive = isCaseInsensitive)
                        yourAnswer.value = it
                    }
                }
                Constants.COMPLETE -> {
                    answers.take(selectedQuestion.value?.answers?.size ?: 0)
                            .map {
                                it.value ?: ""
                            }
                            .let {
                                yourAnswer.value = it.joinToString(separator = "\n")
                                isCorrect = question.isCorrect(it, isCaseInsensitive)
                            }
                }
                Constants.SELECT_COMPLETE -> {
                    selections
                            .take(selectedQuestion.value?.totalSize ?: 0)
                            .map {
                                it.value ?: ""
                            }
                            .filterIndexed { index, it ->
                                checkLists[index].value ?: false
                            }
                            .let {
                                yourAnswer.value = it.joinToString(separator = "\n")
                                isCorrect = question.isCorrect(it, isCaseInsensitive)
                            }
                }
                else -> {
                }
            }

        }

        judgeResult(isCorrect)
    }

    private fun judgeResult(isCorrect: Boolean) {
        if (isCorrect && !preferences.alwaysReview) {
            viewModelScope.launch {
                delay(1000)
                loadNext()
            }
        } else {
            state.value = State.REVIEW
        }

        //todo: activityでやった方が良さそう
        viewModelScope.launch {
            judgeState.value = if (isCorrect) JudgeState.CORRECT else JudgeState.INCORRECT
            delay(1000)
            judgeState.value = JudgeState.NONE
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