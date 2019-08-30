package jp.gr.java_conf.foobar.testmaker.service.activities

import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.models.Test
import jp.gr.java_conf.foobar.testmaker.service.models.TestMakerRepository

class ResultViewModel(private val repository: TestMakerRepository) : ViewModel() {

    fun getTest(testId: Long): Test = repository.getTest(testId)

}