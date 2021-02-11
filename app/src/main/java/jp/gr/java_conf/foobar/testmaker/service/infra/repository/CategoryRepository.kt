package jp.gr.java_conf.foobar.testmaker.service.infra.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import jp.gr.java_conf.foobar.testmaker.service.domain.Category
import jp.gr.java_conf.foobar.testmaker.service.infra.db.CategoryDataSource
import jp.gr.java_conf.foobar.testmaker.service.infra.db.TestDataSource

class CategoryRepository(private val dataSource: CategoryDataSource, private val testDataSource: TestDataSource) {

    private var categories: MutableLiveData<List<Category>> = MutableLiveData(dataSource.get())

    fun get(): LiveData<List<Category>> = categories

    fun get(id: Long): Category = dataSource.get(id)

    fun refresh() {
        categories.value = dataSource.get()
    }

    fun create(category: Category): Long {
        val id = dataSource.create(category)
        refresh()
        return id
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