package jp.gr.java_conf.foobar.testmaker.service.view.edit

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.QuestionRepository

class QuestionViewModel(private val repository: QuestionRepository) : ViewModel() {

    var questionsLiveData: LiveData<List<Question>> = repository.getAsLiveData()

    var questions: List<Question> = repository.get()

    fun refresh() {
        repository.refresh()
    }

    fun create(question: Question): Long = repository.create(question)

    fun update(question: Question) = repository.update(question)

    fun delete(question: Question) {
        repository.delete(question)
    }

    fun swap(from: Question, to: Question) {
        repository.swap(from, to)
    }

}