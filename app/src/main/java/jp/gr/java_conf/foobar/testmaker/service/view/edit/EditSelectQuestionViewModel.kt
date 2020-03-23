package jp.gr.java_conf.foobar.testmaker.service.view.edit

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.domain.Question

class EditSelectQuestionViewModel : EditQuestionViewModel() {

    var selectedQuestion = Question()
        set(value) {
            field = value
            inputForm(field)
        }

    val question = MutableLiveData("")
    val answer = MutableLiveData("")
    val explanation = MutableLiveData("")
    val others = List(SIZE_OTHER_MAX) { MutableLiveData("") }
    val isCheckedImage = MutableLiveData(false)
    val isCheckedExplanation = MutableLiveData(false)
    val isCheckedAuto = MutableLiveData(false)
    val sizeOfOthers = MutableLiveData(3)

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

    fun createQuestion() = selectedQuestion.copy(
            question = question.value ?: "",
            answer = answer.value ?: "",
            others = others.map { it.value ?: "" }.take(sizeOfOthers.value ?: 0),
            explanation = explanation.value ?: "",
            isAutoGenerateOthers = isCheckedAuto.value ?: false,
            type = Constants.SELECT,
            imagePath = imagePath.value ?: ""
    )

    fun formReset() {
        if (isResetForm.value == true) {
            question.value = ""
            answer.value = ""
            explanation.value = ""
            imagePath.value = ""
            others.forEach {
                it.value = ""
            }
        }
    }

    private fun inputForm(question: Question) {
        this.question.value = question.question
        answer.value = question.answer
        sizeOfOthers.value = question.others.size.coerceAtLeast(SIZE_OTHER_MIN)
        explanation.value = question.explanation
        isCheckedExplanation.value = question.explanation.isNotEmpty()
        imagePath.value = question.imagePath
        isCheckedImage.value = question.imagePath.isNotEmpty()
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
            }
        }
    }

    companion object {
        const val SIZE_OTHER_MIN = 1
        const val SIZE_OTHER_MAX = 5
    }
}