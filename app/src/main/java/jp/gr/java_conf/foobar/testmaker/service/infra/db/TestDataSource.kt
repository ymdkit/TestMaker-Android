package jp.gr.java_conf.foobar.testmaker.service.infra.db

import io.realm.Realm
import io.realm.Sort
import jp.gr.java_conf.foobar.testmaker.service.SortTest
import jp.gr.java_conf.foobar.testmaker.service.domain.RealmTest

class TestDataSource(private val realm: Realm) {

    fun create(test: RealmTest): Long {
        test.id = realm.where(RealmTest::class.java).max("id")?.toLong()?.plus(1) ?: 0
        test.order = test.id.toInt()
        realm.executeTransaction {
            it.copyToRealm(test)
        }
        return test.id
    }

    fun get(): List<RealmTest> = realm.copyFromRealm(realm.where(RealmTest::class.java)
            .findAll())
            ?.sortedBy { it.order }
            ?: listOf()

    fun update(test: RealmTest) {
        realm.executeTransaction {
            it.copyToRealmOrUpdate(test)
        }
    }

    fun swap(from: RealmTest, to: RealmTest) {
        val tmp = from.order
        update(from.apply {
            this.order = to.order
        })
        update(to.apply {
            this.order = tmp
        })
    }

    fun delete(test: RealmTest) {
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