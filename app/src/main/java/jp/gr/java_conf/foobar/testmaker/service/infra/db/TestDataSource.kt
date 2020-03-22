package jp.gr.java_conf.foobar.testmaker.service.infra.db

import io.realm.Realm
import io.realm.Sort
import jp.gr.java_conf.foobar.testmaker.service.SortTest
import jp.gr.java_conf.foobar.testmaker.service.domain.Quest
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.domain.RealmTest
import jp.gr.java_conf.foobar.testmaker.service.domain.Test

class TestDataSource(private val realm: Realm) {

    fun create(test: Test): Long {
        val questionId = realm.where(Quest::class.java).max("id")?.toLong()?.plus(1) ?: 0
        val realmTest = RealmTest.createFromTest(test.copy(
                questions = test.questions.mapIndexed { index, question ->
                    question.copy(
                            id = questionId + index,
                            order = index)
                }))

        realmTest.id = realm.where(RealmTest::class.java).max("id")?.toLong()?.plus(1) ?: 0
        realmTest.order = realmTest.id.toInt()
        realm.executeTransaction { realm ->
            realm.copyToRealm(realmTest)
        }
        return realmTest.id
    }

    fun get(): List<Test> = realm.copyFromRealm(realm.where(RealmTest::class.java)
            .findAll())
            ?.sortedBy { it.order }
            ?.map { Test.createFromRealmTest(it) }
            ?: listOf()

    fun get(id: Long): Test = Test.createFromRealmTest(realm.copyFromRealm(realm.where(RealmTest::class.java)
            .equalTo("id", id).findFirst() ?: RealmTest()))

    private fun update(test: RealmTest) {
        realm.executeTransaction {
            it.copyToRealmOrUpdate(test)
        }
    }

    fun update(test: Test) {
        var result = test
        val questionId = realm.where(Quest::class.java).max("id")?.toLong()?.plus(1) ?: 0
        if (test.questions.size >= 2 && test.questions[0].id == test.questions[1].id) {
            result = result.copy(questions =
            result.questions.mapIndexed { index, question ->
                question.copy(
                        id = questionId + index,
                        order = index)
            })
        }

        realm.executeTransaction {
            it.copyToRealmOrUpdate(RealmTest.createFromTest(result))
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

    fun create(test: Test, question: Question): Long {
        val questionId = realm.where(Quest::class.java).max("id")?.toLong()?.plus(1) ?: 0

        update(test.copy(
                questions = test.questions + listOf(question.copy(
                        id = questionId,
                        order = questionId.toInt()
                ))
        ))
        return questionId
    }

    fun update(question: Question) {
        realm.executeTransaction {
            it.copyToRealmOrUpdate(Quest.createQuestFromQuestion(question))
        }
    }

    fun swap(from: Question, to: Question) {
        val tmp = from.order
        update(from.copy(order = to.order))
        update(to.copy(order = tmp))
    }

    fun delete(question: Question) {
        realm.executeTransaction {
            realm.where(Quest::class.java).equalTo("id", question.id).findFirst()?.deleteFromRealm()
        }
    }

}