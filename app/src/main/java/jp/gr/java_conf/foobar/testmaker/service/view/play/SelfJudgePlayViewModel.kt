package jp.gr.java_conf.foobar.testmaker.service.view.play

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager

class SelfJudgePlayViewModel(private val questions: List<Question>, private val preferences: SharedPreferenceManager) : ViewModel() {

    val index = MutableLiveData(0)
    val selectedQuestion = MutableLiveData(Question())

    val state = MutableLiveData(SelfJudgeState.NONE)
    val isReversible = preferences.reverse
    val judgeState = MutableLiveData(SelfJudgeJudgeState.NONE)

    fun loadNext() {
        index.value?.let {
            if (it >= questions.size) {
                state.value = SelfJudgeState.FINISH
            } else {
                val question = questions[it]

                selectedQuestion.value = question
                index.value = it + 1
                state.value = SelfJudgeState.NONE
            }
        }
    }

    fun review() {
        state.value = SelfJudgeState.REVIEW
    }

    fun judge(isCorrect: Boolean) {
        judgeState.value = if (isCorrect) SelfJudgeJudgeState.CORRECT else SelfJudgeJudgeState.INCORRECT
        loadNext()
    }
}

enum class SelfJudgeState {
    NONE,
    REVIEW,
    FINISH
}

enum class SelfJudgeJudgeState {
    NONE,
    CORRECT,
    INCORRECT
}