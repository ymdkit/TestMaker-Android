package jp.gr.java_conf.foobar.testmaker.service.view.edit

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.domain.Question

class EditWriteQuestionViewModel : ViewModel() {

    var testId = -1L
    var selectedQuestion = Question()
        set(value) {
            field = value
            inputForm(field)
        }

    val question = MutableLiveData("")
    val answer = MutableLiveData("")
    val explanation = MutableLiveData("")
    val isCheckedImage = MutableLiveData(false)
    val isCheckedExplanation = MutableLiveData(false)
    val isResetForm = MutableLiveData(true)
    val imagePath = MutableLiveData("")
    val isVisibleSetting = MutableLiveData(false)

    val isValidated = MediatorLiveData<Boolean>().also { result ->
        result.addSource(question) { result.value = isValid }
        result.addSource(answer) { result.value = isValid }
    }

    private val isValid: Boolean
        get() = !question.value.isNullOrEmpty() && !answer.value.isNullOrEmpty()

    fun createQuestion() = selectedQuestion.copy(
            question = question.value ?: "",
            answer = answer.value ?: "",
            explanation = explanation.value ?: "",
            type = Constants.WRITE,
            imagePath = imagePath.value ?: ""
    )

    fun formReset() {
        if (isResetForm.value == true) {
            question.value = ""
            answer.value = ""
            explanation.value = ""
            imagePath.value = ""
        }
    }

    fun onClickSetting() {
        isVisibleSetting.value = !(isVisibleSetting.value ?: false)
    }

    private fun inputForm(question: Question) {
        this.question.value = question.question
        answer.value = question.answer
        explanation.value = question.explanation
        isCheckedExplanation.value = question.explanation.isNotEmpty()
        imagePath.value = question.imagePath
        isCheckedImage.value = question.imagePath.isNotEmpty()
    }
}