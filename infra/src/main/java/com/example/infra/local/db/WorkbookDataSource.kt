package com.example.infra.local.db

import com.example.infra.local.entity.Quest
import com.example.infra.local.entity.RealmTest
import io.realm.Realm
import io.realm.RealmModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkbookDataSource @Inject constructor(
    private val realm: Realm
) {

    private inline fun <reified T : RealmModel> generateId(): Long =
        realm.where(T::class.java).max("id")?.toLong()?.plus(1) ?: 1L

    fun generateWorkbookId(): Long = generateId<RealmTest>()
    fun generateQuestionId(): Long = generateId<Quest>()

    fun createWorkbook(workbook: RealmTest) =
        realm.executeTransaction { realm ->
            realm.copyToRealmOrUpdate(workbook)
        }

    fun getWorkbookList(): List<RealmTest> =
        (realm.copyFromRealm(
            realm.where(RealmTest::class.java)
                .findAll()
        ) ?: listOf()).sortedBy { it.order }

    fun getWorkbook(id: Long): RealmTest =
        realm.copyFromRealm(
            realm.where(RealmTest::class.java)
                .equalTo("id", id).findFirst() ?: RealmTest()
        )

    fun updateQuestion(test: RealmTest) =
        realm.executeTransaction {
            it.copyToRealmOrUpdate(test)
        }

    fun deleteWorkbook(workbookId: Long) =
        realm.executeTransaction {
            realm.where(RealmTest::class.java).equalTo("id", workbookId).findFirst()
                ?.deleteFromRealm()
        }

    fun updateQuestion(question: Quest) =
        realm.executeTransaction {
            it.copyToRealmOrUpdate(question)
        }

    fun deleteQuestion(question: Quest) =
        realm.executeTransaction {
            realm.where(Quest::class.java).equalTo("id", question.id).findFirst()?.deleteFromRealm()
        }
}