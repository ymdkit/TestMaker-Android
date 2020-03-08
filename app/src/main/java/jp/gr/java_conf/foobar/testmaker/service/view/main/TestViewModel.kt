package jp.gr.java_conf.foobar.testmaker.service.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.TestRepository

class TestViewModel(private val repository: TestRepository) : ViewModel() {

    var testsLiveData: LiveData<List<Test>> = repository.getAsLiveData()

    var tests: List<Test> = repository.get()

    fun refresh() {
        repository.refresh()
    }

    fun create(test: Test): Long {
        return repository.create(test)
    }

    fun delete(test: Test) {
        repository.delete(test)
    }

    fun swap(from: Test, to: Test) {
        repository.swap(from, to)
    }

}