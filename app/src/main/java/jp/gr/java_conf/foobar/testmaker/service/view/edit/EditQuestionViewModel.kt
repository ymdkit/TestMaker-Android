package jp.gr.java_conf.foobar.testmaker.service.view.edit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EditQuestionViewModel : ViewModel() {

    var testId = -1L

    val question = MutableLiveData("")
    val answer = MutableLiveData("")
    val explanation = MutableLiveData("")
    val isCheckedImage = MutableLiveData(false)
    val isCheckedExplanation = MutableLiveData(false)


}