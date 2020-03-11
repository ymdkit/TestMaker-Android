package jp.gr.java_conf.foobar.testmaker.service.infra.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.infra.db.QuestionDataSource

class QuestionRepository(private val dataSource: QuestionDataSource) {

    private var questionsLiveData: MutableLiveData<List<Question>> = MutableLiveData(dataSource.get())

    private var questions: List<Question>? = null

    fun getAsLiveData(): LiveData<List<Question>> = questionsLiveData

    fun get(): List<Question> = questions ?: dataSource.get().also {
        questions = it
    }

    fun refresh() {
        questions = dataSource.get()
        questionsLiveData.value = questions
    }

    fun create(question: Question): Long {
        val id = dataSource.create(question)
        refresh()
        return id
    }

    fun update(question: Question) {
        dataSource.update(question)
        refresh()
    }

    fun delete(question: Question) {
        dataSource.delete(question)
        refresh()
    }

    fun swap(from: Question, to: Question) {
        dataSource.swap(from, to)
        refresh()
    }
}