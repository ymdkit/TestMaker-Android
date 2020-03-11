package jp.gr.java_conf.foobar.testmaker.service.infra.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import jp.gr.java_conf.foobar.testmaker.service.SortTest
import jp.gr.java_conf.foobar.testmaker.service.domain.RealmTest
import jp.gr.java_conf.foobar.testmaker.service.infra.db.TestDataSource

class TestRepository(private val dataSource: TestDataSource) {

    private var testsLiveData: MutableLiveData<List<RealmTest>> = MutableLiveData(dataSource.get())

    private var tests: List<RealmTest>? = null

    fun getAsLiveData(): LiveData<List<RealmTest>> = testsLiveData

    fun get(): List<RealmTest> = tests ?: dataSource.get().also {
        tests = it
    }

    fun refresh() {
        tests = dataSource.get()
        testsLiveData.value = tests
    }

    fun create(test: RealmTest): Long {
        val id = dataSource.create(test)
        refresh()
        return id
    }

    fun update(test: RealmTest) {
        dataSource.update(test)
        refresh()
    }

    fun delete(test: RealmTest) {
        dataSource.delete(test)
        refresh()
    }

    fun swap(from: RealmTest, to: RealmTest) {
        dataSource.swap(from, to)
        refresh()
    }

    fun sort(mode: SortTest) {
        dataSource.sort(mode)
        refresh()
    }

}