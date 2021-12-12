package jp.gr.java_conf.foobar.testmaker.service.infra.db

import android.content.Context
import io.realm.Realm
import jp.gr.java_conf.foobar.testmaker.service.domain.Quest
import jp.gr.java_conf.foobar.testmaker.service.domain.RealmTest
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest

class LocalDataSource(private val realm: Realm, private val context: Context) {

    fun getTest(testId: Long): RealmTest {
        return realm.where(RealmTest::class.java).equalTo("id", testId).findFirst() ?: RealmTest()
    }

    fun createObjectFromFirebase(firebaseTest: FirebaseTest, source: String): Test {
        realm.beginTransaction()

        val test = firebaseTest.toTest(context)
        test.id = realm.where(RealmTest::class.java).max("id")?.toLong()?.plus(1) ?: 1
        test.order = test.id.toInt()
        test.source = source

        firebaseTest.questions.forEachIndexed { index, it ->
            val question = it.toQuest()
            question.order = index
            question.id = realm.where(Quest::class.java).max("id")?.toLong()?.plus(1) ?: 1

            realm.copyToRealmOrUpdate(question)
            test.addQuestion(question)
        }

        realm.copyToRealmOrUpdate(test)

        realm.commitTransaction()

        return Test.createFromRealmTest(test)
    }

    fun updateDocumentId(test: RealmTest, documentId: String) {
        realm.beginTransaction()
        test.documentId = documentId
        realm.commitTransaction()
    }

    fun updateDocumentId(question: Quest, documentId: String) {
        realm.beginTransaction()
        question.documentId = documentId
        realm.commitTransaction()
    }
}