package jp.gr.java_conf.foobar.testmaker.service.infra.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import jp.gr.java_conf.foobar.testmaker.service.SortTest
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.db.TestDataSource

class TestRepository(private val dataSource: TestDataSource) {

    private var testsLiveData: MutableLiveData<List<Test>> = MutableLiveData(dataSource.get())

    private var tests: List<Test>? = null

    fun getAsLiveData(): LiveData<List<Test>> = testsLiveData

    fun get(): List<Test> = tests ?: dataSource.get().also {
        tests = it
    }

    fun refresh() {
        tests = dataSource.get()
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

    fun sort(mode: SortTest) {
        dataSource.sort(mode)
        refresh()
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