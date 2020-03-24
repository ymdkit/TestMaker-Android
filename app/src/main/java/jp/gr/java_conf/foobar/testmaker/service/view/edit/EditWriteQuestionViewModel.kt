package jp.gr.java_conf.foobar.testmaker.service.view.edit

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.domain.Question

class EditWriteQuestionViewModel : EditQuestionViewModel() {

    var selectedQuestion = Question()
        set(value) {
            field = value
            inputForm(field)
        }

    val answer = MutableLiveData("")

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

    override fun resetForm() {
        if (isResetForm.value == true) {
            super.resetForm()
            answer.value = ""
        }
    }

    override fun inputForm(question: Question) {
        super.inputForm(question)
        answer.value = question.answer
    }
}