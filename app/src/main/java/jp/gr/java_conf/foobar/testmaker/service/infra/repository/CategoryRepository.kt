package jp.gr.java_conf.foobar.testmaker.service.infra.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.infra.local.source.FolderDataSource
import jp.gr.java_conf.foobar.testmaker.service.domain.Category

class CategoryRepository(
    private val dataSource: FolderDataSource,
) {

    private var categories: MutableLiveData<List<Category>> =
        MutableLiveData(dataSource.get().map { Category.createFromRealmCategory(it) })

    fun get(): List<Category> = dataSource.get().map { Category.createFromRealmCategory(it) }


    fun getAsLiveData(): LiveData<List<Category>> = categories

    fun get(id: Long): Category = Category.createFromRealmCategory(dataSource.get(id))

    fun refresh() {
        categories.value = dataSource.get().map { Category.createFromRealmCategory(it) }
    }

    fun create(category: Category): Long {
        val id = dataSource.create(category.toRealmCategory())
        refresh()
        return id
    }

    fun delete(category: Category) {
        dataSource.delete(category.toRealmCategory())
        refresh()
    }

    fun swap(from: Category, to: Category) {
        val tmp = from.order
        dataSource.update(from.copy(order = to.order).toRealmCategory())
        dataSource.update(to.copy(order = tmp).toRealmCategory())
        refresh()
    }

    fun update(category: Category) {
        dataSource.update(category.toRealmCategory())
        refresh()
    }
}