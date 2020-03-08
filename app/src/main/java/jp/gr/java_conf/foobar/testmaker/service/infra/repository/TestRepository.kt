package jp.gr.java_conf.foobar.testmaker.service.infra.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
        testsLiveData.value = dataSource.get()
    }

    fun create(test: Test): Long {
        val id = dataSource.create(test)
        refresh()
        return id
    }

    fun delete(test: Test) {
        dataSource.delete(test)
        refresh()
    }

    fun swap(from: Test, to: Test) {
        dataSource.swap(from, to)
        refresh()
    }

}