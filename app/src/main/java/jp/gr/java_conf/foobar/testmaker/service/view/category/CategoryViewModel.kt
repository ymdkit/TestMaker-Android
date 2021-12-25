package jp.gr.java_conf.foobar.testmaker.service.view.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import jp.gr.java_conf.foobar.testmaker.service.domain.Category
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.CategoryRepository

class CategoryViewModel(private val repository: CategoryRepository) : ViewModel() {

    @Deprecated("")
    var categoriesLiveData: LiveData<List<Category>> = repository.getAsLiveData()

    fun getCategories() = repository.get()

    fun get(id: Long): Category = repository.get(id)

    fun refresh() {
        repository.refresh()
    }

    fun create(category: Category): Long {
        val id = repository.create(category)
        repository.refresh()
        return id
    }

    fun update(category: Category) = repository.update(category)

    fun delete(category: Category) {
        repository.delete(category)
        repository.refresh()
    }

    fun swap(from: Category, to: Category) {
        repository.swap(from, to)
    }

}