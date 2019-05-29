package jp.gr.java_conf.foobar.testmaker.service.activities

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.Constants

class EditViewModel: ViewModel() {

    val formatQuestion: MutableLiveData<Int> = MutableLiveData()
    val stateEditing: MutableLiveData<Int> = MutableLiveData()
    val spinnerAnswersPosition: MutableLiveData<Int> = MutableLiveData()
    val spinnerSelectsPosition: MutableLiveData<Int> = MutableLiveData()
    val isEditingExplanation: MutableLiveData<Boolean> = MutableLiveData()
    val isAuto: MutableLiveData<Boolean> = MutableLiveData()
    val isCheckOrder: MutableLiveData<Boolean> = MutableLiveData()



    init {
        formatQuestion.value = Constants.WRITE
        stateEditing.value = Constants.NOT_EDITING
        isEditingExplanation.value = false
        isAuto.value = false
        isCheckOrder.value = false
    }

    fun editQuestion(){
        stateEditing.value = Constants.EDIT_QUESTION
    }
}