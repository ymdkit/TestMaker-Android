package jp.gr.java_conf.foobar.testmaker.service.view.share

import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.test.TestMakerRepository

class ShowTestsViewModel(private val repository: TestMakerRepository) : ViewModel() {

    fun getTest(testId: Long): Test = repository.getTest(testId)
    fun deleteTest(test: Test) = repository.deleteTest(test)
    fun updateHistory(test: Test) = repository.updateHistory(test)
    fun updateStart(test: Test, start: Int) = repository.updateStart(test, start)
    fun updateLimit(test: Test, limit: Int) = repository.updateLimit(test, limit)
}