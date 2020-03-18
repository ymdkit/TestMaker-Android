package jp.gr.java_conf.foobar.testmaker.service.view.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.domain.Category
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.CategoryRepository

class CategoryViewModel(private val repository: CategoryRepository) : ViewModel() {

    var categories: LiveData<List<Category>> = repository.get()

    var hasTestsCategories: LiveData<List<Category>> = repository.getHasTests()

    fun refresh() {
        repository.refresh()
    }

    fun create(category: Category) {
        repository.create(category)
        repository.refresh()
    }

    fun delete(category: Category) {
        repository.delete(category)
        repository.refresh()
    }

    fun swap(from: Category, to: Category) {
        repository.swap(from, to)
    }

}