package jp.gr.java_conf.foobar.testmaker.service.view.edit

import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.TestMakerRepository

class EditProViewModel(private val repository: TestMakerRepository) : ViewModel() {

    fun getMaxQuestionId(): Long = repository.getMaxQuestionId()

}