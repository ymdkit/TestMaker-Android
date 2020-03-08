package jp.gr.java_conf.foobar.testmaker.service.infra.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import jp.gr.java_conf.foobar.testmaker.service.domain.Category
import jp.gr.java_conf.foobar.testmaker.service.infra.db.CategoryDataSource
import jp.gr.java_conf.foobar.testmaker.service.infra.db.TestDataSource

class CategoryRepository(private val dataSource: CategoryDataSource, private val testDataSource: TestDataSource) {

    private var categories: MutableLiveData<List<Category>> = MutableLiveData(dataSource.get())

    private var hasTestsCategories: LiveData<List<Category>> = Transformations.map(categories) { it ->
        it.filter { testDataSource.get().map { test -> test.getCategory() }.contains(it.name) }
    }

    fun get(): LiveData<List<Category>> = categories

    fun getHasTests(): LiveData<List<Category>> = hasTestsCategories

    fun refresh() {
        categories.value = dataSource.get()
    }

    fun create(category: Category) {
        dataSource.create(category)
        refresh()
    }

    fun delete(category: Category) {
        dataSource.delete(category)
        refresh()
    }

    fun swap(from: Category, to: Category) {
        dataSource.swap(from, to)
        refresh()
    }

}