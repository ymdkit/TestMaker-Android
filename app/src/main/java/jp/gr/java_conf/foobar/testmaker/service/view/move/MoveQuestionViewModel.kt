package jp.gr.java_conf.foobar.testmaker.service.view.move

import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.domain.Quest
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.TestMakerRepository

class MoveQuestionViewModel(private val repository: TestMakerRepository): ViewModel() {

    fun getTests(): List<Test> = repository.getTests()
    fun addTest(test: Test): Long = repository.addOrUpdateTest(test)
    fun addQuestions(testId: Long, array: Array<Quest>) = repository.addQuestions(testId,array)
    fun deleteQuestions(testId: Long,array: Array<Boolean>) =  repository.deleteQuestions(testId,array)

}