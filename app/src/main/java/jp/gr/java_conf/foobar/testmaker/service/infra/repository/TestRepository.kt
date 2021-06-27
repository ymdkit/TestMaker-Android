package jp.gr.java_conf.foobar.testmaker.service.infra.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.db.TestDataSource

class TestRepository(private val dataSource: TestDataSource) {

    private var testsLiveData: MutableLiveData<List<Test>> = MutableLiveData(dataSource.getAll())

    private var tests: List<Test>? = null

    fun getAsLiveData(): LiveData<List<Test>> = testsLiveData

    fun get(): List<Test> = tests ?: dataSource.getAll().also {
        tests = it
    }

    fun get(id: Long) = dataSource.get(id)

    fun refresh() {
        tests = dataSource.getAll()
        testsLiveData.value = tests
    }

    fun create(test: Test): Long {
        val id = dataSource.create(test)
        refresh()
        return id
    }

    fun update(test: Test) {
        dataSource.update(test)
        refresh()
    }

    fun delete(test: Test) {
        dataSource.delete(test)
        refresh()
    }

    fun swap(from: Test, to: Test) {
        dataSource.swap(from, to)
        refresh()
    }

    fun create(test: Test, question: Question): Long {
        val id = dataSource.create(test, question)
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

    fun insertAt(test: Test, question: Question, index: Int) {
        dataSource.insertAt(test, question, index)
        refresh()
    }

}