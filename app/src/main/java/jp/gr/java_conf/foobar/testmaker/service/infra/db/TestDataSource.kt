package jp.gr.java_conf.foobar.testmaker.service.infra.db

import io.realm.Realm
import io.realm.Sort
import jp.gr.java_conf.foobar.testmaker.service.SortTest
import jp.gr.java_conf.foobar.testmaker.service.domain.RealmTest
import jp.gr.java_conf.foobar.testmaker.service.domain.Test

class TestDataSource(private val realm: Realm) {

    fun create(test: Test): Long {
        val realmTest = RealmTest.createFromTest(test)
        realmTest.id = realm.where(RealmTest::class.java).max("id")?.toLong()?.plus(1) ?: 0
        realmTest.order = test.id.toInt()
        realm.executeTransaction {
            it.copyToRealm(realmTest)
        }
        return realmTest.id
    }

    fun get(): List<Test> = realm.copyFromRealm(realm.where(RealmTest::class.java)
            .findAll())
            ?.sortedBy { it.order }
            ?.map { Test.createFromRealmTest(it) }
            ?: listOf()

    private fun update(test: RealmTest) {
        realm.executeTransaction {
            it.copyToRealmOrUpdate(test)
        }
    }

    fun update(test: Test) {
        realm.executeTransaction {
            it.copyToRealmOrUpdate(RealmTest.createFromTest(test))
        }
    }

    fun swap(from: Test, to: Test) {
        val tmp = from.order
        update(from.copy(order = to.order))
        update(to.copy(order = tmp))
    }

    fun delete(test: Test) {
        realm.executeTransaction {
            realm.where(RealmTest::class.java).equalTo("id", test.id).findFirst()?.deleteFromRealm()
        }
    }

    fun sort(mode: SortTest) {
        val tests = realm.copyFromRealm(realm.where(RealmTest::class.java).findAll().sort("category", Sort.ASCENDING, mode.column, mode.sort))

        tests.forEachIndexed { index, test ->
            update(test.apply {
                order = index
            })
        }
    }

}