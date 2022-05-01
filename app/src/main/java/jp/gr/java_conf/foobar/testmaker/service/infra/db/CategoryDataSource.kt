package jp.gr.java_conf.foobar.testmaker.service.infra.db

import com.example.infra.local.entity.RealmCategory
import io.realm.Realm
import jp.gr.java_conf.foobar.testmaker.service.domain.Category

class CategoryDataSource(private val realm: Realm) {

    fun create(category: Category): Long {
        val result = category.toRealmCategory()
        result.id = realm.where(RealmCategory::class.java).max("id")?.toLong()?.plus(1) ?: 0
        result.order = result.id.toInt()
        realm.executeTransaction {
            it.copyToRealm(result)
        }
        return result.id
    }

    fun get(): List<Category> = realm.copyFromRealm(realm.where(RealmCategory::class.java)
            .findAll())
            ?.map { Category.createFromRealmCategory(it) }
            ?.distinctBy { it.name }
            ?.sortedBy { it.order }
            ?: listOf()

    fun get(id: Long): Category = Category.createFromRealmCategory(realm.copyFromRealm(realm.where(RealmCategory::class.java)
            .equalTo("id", id)
            .findFirst()
            ?: RealmCategory()
    ))

    fun update(category: Category) {
        realm.executeTransaction {
            it.copyToRealmOrUpdate(category.toRealmCategory())
        }
    }

    fun swap(from: Category, to: Category) {
        val tmp = from.order
        update(from.copy(order = to.order))
        update(to.copy(order = tmp))
    }

    fun delete(category: Category) {
        realm.executeTransaction {
            realm.where(RealmCategory::class.java).equalTo("id", category.id).findFirst()?.deleteFromRealm()
        }
    }

}