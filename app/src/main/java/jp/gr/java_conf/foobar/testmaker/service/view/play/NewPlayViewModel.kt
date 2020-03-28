package jp.gr.java_conf.foobar.testmaker.service.view.play

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.domain.Test

class NewPlayViewModel(private val test: Test) : ViewModel() {

    val index = MutableLiveData(0)

    val selectedQuestion = MutableLiveData(Question())

    val answer = MutableLiveData("")
    val answers = List(COMPLETE_ANSWER_MAX) { MutableLiveData("") }
    val selections = List(SELECTION_MAX) { MutableLiveData("") }

    val state = MutableLiveData(State.INITIAL)

    val finish = MutableLiveData<Unit>()

    fun loadNext() {
        index.value?.let {
            if (it >= test.questions.size) {

                finish.value = Unit

            } else {
                selectedQuestion.value = test.questions[it]
                index.value = it + 1
                state.value = State.getStateFromType(test.questions[it])
                // todo 自動生成モードの対応 選択完答に対応
                selections.forEach { it.value = "" }
                (test.questions[it].others + listOf(test.questions[it].answer)).shuffled().forEachIndexed { index, it ->
                    selections[index].value = it
                }
            }
        }
    }

    fun judge() {
        selectedQuestion.value?.let {
            when (it.type) {
                Constants.WRITE -> {
                    answer.value?.let {
                        state.value = State.REVIEW
                    }
                }
                Constants.SELECT -> {
                    answer.value?.let {
                        state.value = State.REVIEW
                    }
                }
                Constants.COMPLETE -> {
                    answer.value?.let {
                        state.value = State.REVIEW
                    }
                }
                Constants.SELECT_COMPLETE -> {
                    answer.value?.let {
                        state.value = State.REVIEW
                    }
                }
                else -> {
                }
            }
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
    REVIEW;

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