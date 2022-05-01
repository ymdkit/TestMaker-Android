package com.example.infra.local.db

import com.example.infra.local.entity.RealmCategory
import io.realm.Realm
import io.realm.RealmModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderDataSource @Inject constructor(
    private val realm: Realm
) {

    private inline fun <reified T : RealmModel> generateId(): Long =
        realm.where(T::class.java).max("id")?.toLong()?.plus(1) ?: 1L

    fun generateFolderId(): Long = generateId<RealmCategory>()

    fun createFolder(folder: RealmCategory) =
        realm.executeTransaction {
            it.copyToRealm(folder)
        }

    fun getFolderList(): List<RealmCategory> =
        realm.copyFromRealm(
            realm.where(RealmCategory::class.java)
                .findAll()
        )
            ?.distinctBy { it.name }
            ?.sortedBy { it.order }
            ?: listOf()

    fun getFolder(folderId: Long): RealmCategory =
        realm.copyFromRealm(
            realm.where(RealmCategory::class.java)
                .equalTo("id", folderId)
                .findFirst()
                ?: RealmCategory()
        )

    fun updateFolder(folder: RealmCategory) {
        realm.executeTransaction {
            it.copyToRealmOrUpdate(folder)
        }
    }

    fun delete(folder: RealmCategory) {
        realm.executeTransaction {
            realm.where(RealmCategory::class.java).equalTo("id", folder.id).findFirst()
                ?.deleteFromRealm()
        }
    }

}