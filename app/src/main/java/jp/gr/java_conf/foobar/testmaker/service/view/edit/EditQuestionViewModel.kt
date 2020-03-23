package jp.gr.java_conf.foobar.testmaker.service.view.edit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class EditQuestionViewModel : ViewModel() {

    var testId = -1L

    val isResetForm = MutableLiveData(true)
    val imagePath = MutableLiveData("")
    val isVisibleSetting = MutableLiveData(false)

    fun onClickSetting() {
        isVisibleSetting.value = !(isVisibleSetting.value ?: false)
    }
}