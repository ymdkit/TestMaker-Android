package jp.gr.java_conf.foobar.testmaker.service.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.SortTest
import jp.gr.java_conf.foobar.testmaker.service.domain.RealmTest
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.TestRepository

class TestViewModel(private val repository: TestRepository) : ViewModel() {

    var testsLiveData: LiveData<List<RealmTest>> = repository.getAsLiveData()

    var tests: List<RealmTest> = repository.get()

    fun refresh() {
        repository.refresh()
    }

    fun create(title: String, color: Int, category: String): Long {
        return repository.create(RealmTest().apply {
            this.title = title
            this.color = color
            this.setCategory(category)
        })
    }

    fun create(test: RealmTest): Long = repository.create(test)

    fun update(test: RealmTest, title: String, color: Int, category: String) {
        repository.update(test.apply {
            this.title = title
            this.color = color
            this.setCategory(category)
        })
    }

    fun update(test: RealmTest) = repository.update(test)


    fun delete(test: RealmTest) {
        repository.delete(test)
    }

    fun swap(from: RealmTest, to: RealmTest) {
        repository.swap(from, to)
    }

    fun sort(mode: SortTest) {
        repository.sort(mode)
    }

}