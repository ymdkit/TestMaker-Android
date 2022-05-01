package com.example.infra.local.source

import com.example.infra.local.entity.RealmCategory
import io.realm.Realm

class FolderDataSource(private val realm: Realm) {

    fun create(category: RealmCategory): Long {
        category.id = realm.where(RealmCategory::class.java).max("id")?.toLong()?.plus(1) ?: 0
        category.order = category.id.toInt()
        realm.executeTransaction {
            it.copyToRealm(category)
        }
        return category.id
    }

    fun get(): List<RealmCategory> = realm.copyFromRealm(
        realm.where(RealmCategory::class.java)
            .findAll()
    )
        ?.distinctBy { it.name }
        ?.sortedBy { it.order }
        ?: listOf()

    fun get(id: Long): RealmCategory = realm.copyFromRealm(
        realm.where(RealmCategory::class.java)
            .equalTo("id", id)
            .findFirst()
            ?: RealmCategory()
    )

    fun update(category: RealmCategory) {
        realm.executeTransaction {
            it.copyToRealmOrUpdate(category)
        }
    }

    fun delete(category: RealmCategory) {
        realm.executeTransaction {
            realm.where(RealmCategory::class.java).equalTo("id", category.id).findFirst()
                ?.deleteFromRealm()
        }
    }

}