package jp.gr.java_conf.foobar.testmaker.service.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.models.Test

class MainViewModel: ViewModel() {

    val title: MutableLiveData<String> = MutableLiveData()
    var isEditing: MutableLiveData<Boolean> = MutableLiveData()

//    fun getTests(): LiveData<List<Test>> {
//        return repository.getTimeLineWorks()
//    }

}