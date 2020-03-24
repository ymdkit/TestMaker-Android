package jp.gr.java_conf.foobar.testmaker.service.view.edit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager

open class EditQuestionViewModel(val preferences: SharedPreferenceManager) : ViewModel() {
    open var selectedQuestion = Question()

    var testId = -1L

    val question = MutableLiveData("")
    val explanation = MutableLiveData("")
    val isResetForm = MutableLiveData(preferences.isResetForm)
    val isCheckedImage = MutableLiveData(preferences.isShowImageSetting)
    val isCheckedExplanation = MutableLiveData(preferences.explanation)
    val imagePath = MutableLiveData("")
    val isVisibleSetting = MutableLiveData(false)

    open fun resetForm() {
        question.value = ""
        explanation.value = ""
        imagePath.value = ""
    }

    open fun inputForm(question: Question) {
        this.question.value = question.question
        explanation.value = question.explanation
        isCheckedExplanation.value = question.explanation.isNotEmpty()
        imagePath.value = question.imagePath
        isCheckedImage.value = question.imagePath.isNotEmpty()
    }

    open fun createQuestion(): Question = Question()

    fun onClickSetting() {
        isVisibleSetting.value = !(isVisibleSetting.value ?: false)
    }
}