package jp.gr.java_conf.foobar.testmaker.service.infra.db

import io.realm.Realm
import jp.gr.java_conf.foobar.testmaker.service.domain.Cate

class CategoryDataSource(private val realm: Realm){

    fun create(category: Cate){
        realm.executeTransaction {
            it.copyToRealm(category)
        }
    }

    fun get(): List<Cate> = realm.copyFromRealm(realm.where(Cate::class.java).findAll())?.sortedBy { it.order } ?: listOf()

    fun delete(category: Cate){
        realm.executeTransaction {
            realm.where(Cate::class.java).equalTo("category", category.category).findFirst()?.deleteFromRealm()
        }
    }

}