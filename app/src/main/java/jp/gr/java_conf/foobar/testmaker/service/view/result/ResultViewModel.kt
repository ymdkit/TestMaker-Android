package jp.gr.java_conf.foobar.testmaker.service.view.result

import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.domain.RealmTest
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.TestMakerRepository

class ResultViewModel(private val repository: TestMakerRepository) : ViewModel() {

    fun getTest(testId: Long): RealmTest = repository.getTest(testId)

}