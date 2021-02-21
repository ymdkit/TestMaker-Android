package jp.gr.java_conf.foobar.testmaker.service.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.SortTest
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.TestRepository

class TestViewModel(private val repository: TestRepository) : ViewModel() {

    var testsLiveData: LiveData<List<Test>> = repository.getAsLiveData()

    var tests: List<Test> = repository.get()

    fun get(id: Long) = repository.get(id)

    fun refresh() {
        repository.refresh()
    }

    fun create(title: String, color: Int, category: String?): Long {
        return repository.create(Test(
                title = title,
                color = color,
                category = category ?: ""
        ))
    }

    fun create(test: Test): Long = repository.create(test)

    fun update(test: Test, title: String, color: Int, category: String?) {
        repository.update(test.copy(title = title, color = color, category = category ?: ""))
    }

    fun update(test: Test) = repository.update(test)

    fun delete(test: Test) {
        repository.delete(test)
    }

    fun swap(from: Test, to: Test) {
        repository.swap(from, to)
    }

    fun sort(mode: SortTest) {
        repository.sort(mode)
    }

    fun create(test: Test, question: Question): Long = repository.create(test, question)

    fun update(question: Question) = repository.update(question)

    fun delete(question: Question) {
        repository.delete(question)
    }

    fun swap(from: Question, to: Question) {
        repository.swap(from, to)
    }

    fun insertAt(test: Test, question: Question, index: Int) {
        repository.insertAt(test, question, index)
    }

    fun deleteAllInCategory(name: String) {
        repository.refresh()
        repository.get().filter {
            it.category == name
        }.forEach {
            repository.delete(it)
        }
    }

    fun renameAllInCategory(oldCategory: String, newCategory: String) {
        repository.refresh()
        repository.get().filter {
            it.category == oldCategory
        }.forEach {
            repository.update(it.copy(category = newCategory))
        }

    }
}