package jp.gr.java_conf.foobar.testmaker.service.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.models.Test
import jp.gr.java_conf.foobar.testmaker.service.models.TestMakerRepository

class MainViewModel(private val repository: TestMakerRepository): ViewModel() {

    val title: MutableLiveData<String> = MutableLiveData()
    var isEditing: MutableLiveData<Boolean> = MutableLiveData()

}