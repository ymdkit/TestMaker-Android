package com.example.infra.local.db

import com.example.infra.local.entity.Quest
import com.example.infra.local.entity.RealmTest
import io.realm.Realm
import io.realm.RealmModel

class TestDataSource(private val realm: Realm) {

    private inline fun <reified T : RealmModel> generateId(): Long =
        realm.where(T::class.java).max("id")?.toLong()?.plus(1) ?: 1L

    fun generateWorkbookId(): Long = generateId<RealmTest>()
    fun generateQuestionId(): Long = generateId<Quest>()

    fun create(test: RealmTest): Long {
        test.id = generateId<RealmTest>()
        test.order = test.id.toInt()
        realm.executeTransaction { realm ->
            realm.copyToRealm(test)
        }
        return test.id
    }

    fun getAll(): List<RealmTest> = realm.copyFromRealm(
        realm.where(RealmTest::class.java)
            .findAll()
    )
        ?.sortedBy { it.order }
        ?: listOf()

    fun get(id: Long): RealmTest =
        realm.copyFromRealm(
            realm.where(RealmTest::class.java)
                .equalTo("id", id).findFirst() ?: RealmTest()
        )

    fun delete(test: RealmTest) {
        realm.executeTransaction {
            realm.where(RealmTest::class.java).equalTo("id", test.id).findFirst()?.deleteFromRealm()
        }
    }

    fun update(question: Quest) {
        realm.executeTransaction {
            it.copyToRealmOrUpdate(question)
        }
    }

    fun delete(question: Quest) {
        realm.executeTransaction {
            realm.where(Quest::class.java).equalTo("id", question.id).findFirst()?.deleteFromRealm()
        }
    }

    fun update(test: RealmTest) {
        realm.executeTransaction {
            it.copyToRealmOrUpdate(test)
        }
    }
}