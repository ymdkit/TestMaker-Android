package jp.gr.java_conf.foobar.testmaker.service.view.play

import android.view.View
import android.widget.Button
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayViewModel(private val test: Test, private val questions: List<Question>, private val preferences: SharedPreferenceManager) : ViewModel() {

    val index = MutableLiveData(0)
    val selectedQuestion = MutableLiveData(Question())

    val answer = MutableLiveData("")
    val answers = List(COMPLETE_ANSWER_MAX) { MutableLiveData("") }
    val selections = List(SELECTION_MAX) { MutableLiveData("") }
    val checkLists = List(SELECTION_MAX) { MutableLiveData(false) }
    val lastCheckedTimes = List(SELECTION_MAX) { MutableLiveData(0L) }
    val checkListOrders = List(SELECTION_MAX) { i ->
        MediatorLiveData<Int>().also { result ->
            lastCheckedTimes.forEach {
                result.addSource(it) {
                    result.value = lastCheckedTimes
                            .filterIndexed { index, mutableLiveData ->
                                checkLists[index].value ?: false
                            }
                            .map { it.value ?: 0L }
                            .sorted()
                            .indexOf(lastCheckedTimes[i].value) + 1
                }
            }
        }
    }


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

                formReset()

                selectedQuestion.value = question
                index.value = it + 1
                state.value = State.getStateFromType(question)
                if (isReversible) state.value = State.WRITE
                if (question.isAutoGenerateOthers) {
                    when (question.type) {
                        Constants.SELECT -> {
                            (test.getChoices(question.others.size, question.answer) + listOf(question.answer)).shuffled().forEachIndexed { index, it ->
                                selections[index].value = it
                            }
                        }
                        Constants.SELECT_COMPLETE -> {
                            (test.getChoices(question.others.size, question.answer) + question.answers).shuffled().forEachIndexed { index, it ->
                                selections[index].value = it
                            }
                        }
                    }

                } else {
                    when (question.type) {
                        Constants.SELECT -> {
                            (question.others + listOf(question.answer)).shuffled().take(SELECTION_MAX).forEachIndexed { index, it ->
                                selections[index].value = it
                            }
                        }
                        Constants.SELECT_COMPLETE -> {
                            (question.others + question.answers).shuffled().take(SELECTION_MAX).forEachIndexed { index, it ->
                                selections[index].value = it
                            }
                        }
                    }

                }
            }
        }
    }

    private fun formReset() {
        judgeState.value = JudgeState.NONE
        answer.value = ""
        answers.forEach { it.value = "" }
        checkLists.forEach { it.value = false }
        selections.forEach { it.value = "" }
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
        selectedQuestion.value?.let { question ->

            if (state.value == State.WRITE) { //todo 問題と回答の入れ替えに対応
                answer.value?.let {
                    yourAnswer.value = it
                    judgeResult(question.isCorrect(it, isReversible, isCaseInsensitive = isCaseInsensitive))
                }
            } else {
                when (question.type) {
                    Constants.WRITE -> {
                        answer.value?.let {
                            yourAnswer.value = it
                            judgeResult(question.isCorrect(it, isReversible, isCaseInsensitive = isCaseInsensitive))
                        }
                    }
                    Constants.COMPLETE -> {
                        answers.take(selectedQuestion.value?.answers?.size ?: 0)
                                .map {
                                    it.value ?: ""
                                }
                                .let {
                                    yourAnswer.value = it.joinToString(separator = "\n")
                                    judgeResult(question.isCorrect(it, isCaseInsensitive))
                                }
                    }
                    Constants.SELECT_COMPLETE -> {

                        val list = List(SELECTION_MAX) { i ->
                            PlaySelectCompleteSelection(
                                    selections[i].value ?: "",
                                    lastCheckedTimes[i].value ?: 0L,
                                    checkLists[i].value ?: false)
                        }

                        list
                                .filter { it.checked }
                                .sortedBy { it.lastCheckedTime }
                                .let {
                                    yourAnswer.value = it.joinToString(separator = "\n") { it.content }
                                    judgeResult(question.isCorrect(it.map { it.content }, isCaseInsensitive))
                                }
                    }
                    else -> {
                    }
                }
            }
        }
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