package jp.gr.java_conf.foobar.testmaker.service.activities

import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.models.Quest
import jp.gr.java_conf.foobar.testmaker.service.models.Test
import jp.gr.java_conf.foobar.testmaker.service.models.TestMakerRepository

class MoveQuestionViewModel(private val repository: TestMakerRepository): ViewModel() {

    fun getTests(): List<Test> = repository.getTests()
    fun addTest(test: Test): Long = repository.addTest(test)
    fun addQuestions(testId: Long, array: Array<Quest>) = repository.addQuestions(testId,array)
    fun deleteQuestions(testId: Long,array: Array<Boolean>) =  repository.deleteQuestions(testId,array)

}