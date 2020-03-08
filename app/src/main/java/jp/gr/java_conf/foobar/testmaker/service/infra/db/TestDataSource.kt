package jp.gr.java_conf.foobar.testmaker.service.infra.db

import io.realm.Realm
import jp.gr.java_conf.foobar.testmaker.service.domain.Test

class TestDataSource(private val realm: Realm) {

    fun create(test: Test) {
        test.id = realm.where(Test::class.java).max("id")?.toLong()?.plus(1) ?: 0
        test.order = test.id.toInt()
        realm.executeTransaction {
            it.copyToRealm(test)
        }
    }

    fun get(): List<Test> = realm.copyFromRealm(realm.where(Test::class.java)
            .findAll())
            ?.sortedBy { it.order }
            ?: listOf()

    fun update(test: Test) {
        realm.executeTransaction {
            it.copyToRealmOrUpdate(test)
        }
    }

    fun swap(from: Test, to: Test) {
        val tmp = from.order
        update(from.apply {
            this.order = to.order
        })
        update(to.apply {
            this.order = tmp
        })
    }

    fun delete(test: Test) {
        realm.executeTransaction {
            realm.where(Test::class.java).equalTo("id", test.id).findFirst()?.deleteFromRealm()
        }
    }

}