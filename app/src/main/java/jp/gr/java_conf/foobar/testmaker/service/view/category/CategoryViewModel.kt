package jp.gr.java_conf.foobar.testmaker.service.view.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.gr.java_conf.foobar.testmaker.service.domain.Category
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.CategoryRepository
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: CategoryRepository
) : ViewModel() {

    var categoriesLiveData: LiveData<List<Category>> = repository.getAsLiveData()

    fun getCategories() = repository.get()

    fun get(id: Long): Category = repository.get(id)

    fun refresh() {
        repository.refresh()
    }

    fun create(category: Category) {
        repository.create(category)
        repository.refresh()
    }

    fun swap(from: Category, to: Category) {
        repository.swap(from, to)
    }

}