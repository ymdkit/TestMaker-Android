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
                else -> {
                }
            }
        }
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