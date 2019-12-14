package jp.gr.java_conf.foobar.testmaker.service.view.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.test.TestMakerRepository

class CategorizedViewModel(private val repository: TestMakerRepository): ViewModel() {

    fun getCategorizedTests(category: String): LiveData<List<Test>> = repository.getCategorizedTestsOfLiveData(category)
    fun fetchCategorizedTests(category: String) = repository.fetchCategorisedTests(category)
    fun getTests() = repository.getTestsOfLiveData()

}