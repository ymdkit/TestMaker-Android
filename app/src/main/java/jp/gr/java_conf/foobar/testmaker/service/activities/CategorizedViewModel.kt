package jp.gr.java_conf.foobar.testmaker.service.activities

import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.models.Test
import jp.gr.java_conf.foobar.testmaker.service.models.TestMakerRepository

class CategorizedViewModel(private val repository: TestMakerRepository): ViewModel() {

    fun getTests(): List<Test> = repository.getTests()
    fun getCategorizedTests(category: String): List<Test> = repository.getCategorizedTests(category)

}