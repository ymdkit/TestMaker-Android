package jp.gr.java_conf.foobar.testmaker.service.view.edit

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager

class EditSelectQuestionViewModel(preferences: SharedPreferenceManager) : EditQuestionViewModel(preferences) {

    override var selectedQuestion = Question()
        set(value) {
            field = value
            inputForm(field)
        }

    val answer = MutableLiveData("")
    val others = List(SIZE_OTHER_MAX) { MutableLiveData("") }
    val isCheckedAuto = MutableLiveData(false)
    val sizeOfOthers = MutableLiveData(preferences.numOthers)

    val isValidated = MediatorLiveData<Boolean>().also { result ->
        result.addSource(question) { result.value = isValid }
        result.addSource(answer) { result.value = isValid }
        others.forEach {
            result.addSource(it) { result.value = isValid }
        }
        result.addSource(sizeOfOthers) { result.value = isValid }
    }

    private val isValid: Boolean
        get() = !question.value.isNullOrEmpty() &&
                !answer.value.isNullOrEmpty() &&
                !others.take(sizeOfOthers.value ?: SIZE_OTHER_MAX).any {
                    it.value.isNullOrEmpty()
                }

    override fun createQuestion() = selectedQuestion.copy(
            question = question.value ?: "",
            answer = answer.value ?: "",
            others = others.map { it.value ?: "" }.take(sizeOfOthers.value ?: 0),
            explanation = explanation.value ?: "",
            isAutoGenerateOthers = isCheckedAuto.value ?: false,
            type = Constants.SELECT,
            imagePath = imagePath.value ?: ""
    )

    override fun resetForm() {
        if (isResetForm.value == true) {
            super.resetForm()
            answer.value = ""
            others.forEach {
                it.value = ""
            }
        }
    }

    override fun inputForm(question: Question) {
        super.inputForm(question)
        answer.value = question.answer
        sizeOfOthers.value = question.others.size.coerceAtLeast(SIZE_OTHER_MIN)
        isCheckedAuto.value = question.isAutoGenerateOthers
        question.others.forEachIndexed { index, s ->
            if (index >= SIZE_OTHER_MAX) return@forEachIndexed
            others[index].value = s
        }
    }

    fun mutateSizeOfOthers(num: Int) {
        sizeOfOthers.value?.let {
            if (it + num in SIZE_OTHER_MIN..SIZE_OTHER_MAX) {
                sizeOfOthers.value = it + num
                preferences.numOthers = it + num
            }
        }
    }

    companion object {
        const val SIZE_OTHER_MIN = 1
        const val SIZE_OTHER_MAX = 5
    }
}