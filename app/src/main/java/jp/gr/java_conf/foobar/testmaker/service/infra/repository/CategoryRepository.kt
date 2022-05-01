package jp.gr.java_conf.foobar.testmaker.service.infra.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.infra.local.db.FolderDataSource
import jp.gr.java_conf.foobar.testmaker.service.domain.Category

class CategoryRepository(
    private val dataSource: FolderDataSource,
) {

    private var categories: MutableLiveData<List<Category>> =
        MutableLiveData(dataSource.getFolderList().map { Category.createFromRealmCategory(it) })

    fun get(): List<Category> =
        dataSource.getFolderList().map { Category.createFromRealmCategory(it) }


    fun getAsLiveData(): LiveData<List<Category>> = categories

    fun get(id: Long): Category = Category.createFromRealmCategory(dataSource.getFolder(id))

    fun refresh() {
        categories.value = dataSource.getFolderList().map { Category.createFromRealmCategory(it) }
    }

    fun create(category: Category) {
        val folderId = dataSource.generateFolderId()

        dataSource.createFolder(
            category.copy(
                id = folderId,
                order = folderId.toInt()
            ).toRealmCategory()
        )
        refresh()
    }

    fun delete(category: Category) {
        dataSource.delete(category.toRealmCategory())
        refresh()
    }

    fun swap(from: Category, to: Category) {
        val tmp = from.order
        dataSource.updateFolder(from.copy(order = to.order).toRealmCategory())
        dataSource.updateFolder(to.copy(order = tmp).toRealmCategory())
        refresh()
    }

    fun update(category: Category) {
        dataSource.updateFolder(category.toRealmCategory())
        refresh()
    }
}