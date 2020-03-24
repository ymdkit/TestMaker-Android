package jp.gr.java_conf.foobar.testmaker.service.view.edit

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.domain.Question

class EditCompleteQuestionViewModel : EditQuestionViewModel() {

    var selectedQuestion = Question()
        set(value) {
            field = value
            inputForm(field)
        }

    val answers = List(SIZE_ANSWER_MAX) { MutableLiveData("") }
    val isCheckedCheckOrder = MutableLiveData(false)
    val sizeOfAnswers = MutableLiveData(3)

    val isValidated = MediatorLiveData<Boolean>().also { result ->
        result.addSource(question) { result.value = isValid }
        answers.forEach {
            result.addSource(it) { result.value = isValid }
        }
        result.addSource(sizeOfAnswers) { result.value = isValid }
    }

    private val isValid: Boolean
        get() = !question.value.isNullOrEmpty() &&
                !answers.take(sizeOfAnswers.value ?: SIZE_ANSWER_MAX).any {
                    it.value.isNullOrEmpty()
                }

    fun createQuestion() = selectedQuestion.copy(
            question = question.value ?: "",
            answer = answers.map { it.value ?: "" }.take(sizeOfAnswers.value
                    ?: 0).joinToString(separator = " "),
            answers = answers.map { it.value ?: "" }.take(sizeOfAnswers.value ?: 0),
            explanation = explanation.value ?: "",
            isCheckOrder = isCheckedCheckOrder.value ?: false,
            type = Constants.COMPLETE,
            imagePath = imagePath.value ?: ""
    )

    override fun resetForm() {
        if (isResetForm.value == true) {
            super.resetForm()
            answers.forEach {
                it.value = ""
            }
        }
    }

    override fun inputForm(question: Question) {
        super.inputForm(question)
        sizeOfAnswers.value = question.answers.size.coerceAtLeast(SIZE_ANSWER_MIN)
        isCheckedCheckOrder.value = question.isCheckOrder
        question.answers.forEachIndexed { index, s ->
            if (index >= SIZE_ANSWER_MAX) return@forEachIndexed
            answers[index].value = s
        }
    }

    fun mutateSizeOfOthers(num: Int) {
        sizeOfAnswers.value?.let {
            if (it + num in SIZE_ANSWER_MIN..SIZE_ANSWER_MAX) {
                sizeOfAnswers.value = it + num
            }
        }
    }

    companion object {
        const val SIZE_ANSWER_MIN = 2
        const val SIZE_ANSWER_MAX = 4
    }
}