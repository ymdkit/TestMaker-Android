package jp.gr.java_conf.foobar.testmaker.service.view.main

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.TestRepository
import javax.inject.Inject

@HiltViewModel
class TestViewModel @Inject constructor(
    private val repository: TestRepository
) : ViewModel() {

    var tests: List<Test> = repository.get()

    fun get(id: Long) = repository.get(id)

    fun refresh() {
        repository.refresh()
    }

    fun create(test: Test) = repository.create(test)

    fun create(test: Test, question: Question): Long = repository.create(test, question)

    fun update(question: Question) = repository.update(question)

    fun delete(question: Question) {
        repository.delete(question)
    }
}