package jp.gr.java_conf.foobar.testmaker.service.infra.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.db.TestDataSource

class TestRepository(private val dataSource: TestDataSource) {

    private var tests: MutableLiveData<List<Test>> = MutableLiveData(dataSource.get())

    fun get(): LiveData<List<Test>> = tests

    fun refresh() {
        tests.value = dataSource.get()
    }

    fun create(test: Test) {
        dataSource.create(test)
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

}