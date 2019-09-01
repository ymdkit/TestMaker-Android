package jp.gr.java_conf.foobar.testmaker.service.view.category

import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.test.TestMakerRepository

class CategorizedViewModel(private val repository: TestMakerRepository): ViewModel() {

    fun getTests(): List<Test> = repository.getTests()
    fun getCategorizedTests(category: String): List<Test> = repository.getCategorizedTests(category)

}