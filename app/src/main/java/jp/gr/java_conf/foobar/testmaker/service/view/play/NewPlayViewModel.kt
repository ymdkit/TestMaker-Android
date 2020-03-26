package jp.gr.java_conf.foobar.testmaker.service.view.play

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.domain.Question

class NewPlayViewModel : ViewModel() {

    val index = MutableLiveData(0)

    val selectedQuestion = MutableLiveData(Question())

    val answer = MutableLiveData("")

}