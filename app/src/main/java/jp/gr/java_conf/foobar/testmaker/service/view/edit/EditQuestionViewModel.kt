package jp.gr.java_conf.foobar.testmaker.service.view.edit

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.domain.Question

class EditQuestionViewModel : ViewModel() {

    var testId = -1L

    val question = MutableLiveData("")
    val answer = MutableLiveData("")
    val explanation = MutableLiveData("")
    val isCheckedImage = MutableLiveData(false)
    val isCheckedExplanation = MutableLiveData(false)
    val isResetForm = MutableLiveData(true)
    val imagePath = MutableLiveData("")

    val isValidated = MediatorLiveData<Boolean>().also { result ->
        result.addSource(question) { result.value = isValid }
        result.addSource(answer) { result.value = isValid }
    }

    private val isValid: Boolean
        get() = !question.value.isNullOrEmpty() && !answer.value.isNullOrEmpty()

    fun createQuestion() = Question(
            question = question.value ?: "",
            answer = answer.value ?: "",
            explanation = explanation.value ?: "",
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
}