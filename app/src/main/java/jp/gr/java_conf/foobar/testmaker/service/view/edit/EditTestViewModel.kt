package jp.gr.java_conf.foobar.testmaker.service.view.edit

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EditTestViewModel : ViewModel() {
    val titleTest = MutableLiveData("")

    val isValidated = MediatorLiveData<Boolean>().also { result ->
        result.addSource(titleTest) { result.value = it.isNotEmpty() }
    }
}