package jp.gr.java_conf.foobar.testmaker.service.view.main

import androidx.lifecycle.LiveData
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

    var testsLiveData: LiveData<List<Test>> = repository.getAsLiveData()

    var tests: List<Test> = repository.get()

    fun get(id: Long) = repository.get(id)

    fun refresh() {
        repository.refresh()
    }

    fun create(title: String, color: Int, category: String?, source: String) =
        repository.create(
            Test(
                title = title,
                color = color,
                category = category ?: "",
                source = source
            )
        )

    fun create(test: Test) = repository.create(test)

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

    fun create(test: Test, question: Question): Long = repository.create(test, question)

    fun update(question: Question) = repository.update(question)

    fun delete(question: Question) {
        repository.delete(question)
    }

    fun delete(questions: List<Question>) {
        questions.forEach {
            delete(it)
        }
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

    fun move(questions: List<Question>, dest: Test) {
        questions.forEach {
            create(dest, it)
            delete(it)
        }
    }

    fun copy(questions: List<Question>, dest: Test) {
        questions.forEach {
            create(dest, it)
        }
    }
}