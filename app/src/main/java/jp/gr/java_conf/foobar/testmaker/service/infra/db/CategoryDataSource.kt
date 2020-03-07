package jp.gr.java_conf.foobar.testmaker.service.infra.db

import io.realm.Realm
import jp.gr.java_conf.foobar.testmaker.service.domain.Cate
import jp.gr.java_conf.foobar.testmaker.service.domain.Category

class CategoryDataSource(private val realm: Realm) {

    init {
        realm.executeTransaction {
            realm.where(Cate::class.java).findAll().forEachIndexed { index, it ->
                val category = Category()
                category.id = index.toLong()
                category.name = it.category
                category.color = it.color
                category.order = it.order
                realm.copyToRealm(category)
                it.deleteFromRealm()
            }
        }
    }

    fun create(category: Category) {
        category.id = realm.where(Category::class.java).max("id")?.toLong()?.plus(1) ?: 0
        category.order = category.id.toInt()
        realm.executeTransaction {
            it.copyToRealm(category)
        }
    }

    fun get(): List<Category> = realm.copyFromRealm(realm.where(Category::class.java)
            .findAll())
            ?.distinctBy { it.name }
            ?.sortedBy { it.order }
            ?: listOf()

    fun update(category: Category) {
        realm.executeTransaction {
            it.copyToRealmOrUpdate(category)
        }
    }

    fun swap(from: Category, to: Category) {
        val tmp = from.order
        update(from.apply {
            this.order = to.order
        })
        update(to.apply {
            this.order = tmp
        })
    }

    fun delete(category: Category) {
        realm.executeTransaction {
            realm.where(Category::class.java).equalTo("name", category.name).findFirst()?.deleteFromRealm()
        }
    }

}