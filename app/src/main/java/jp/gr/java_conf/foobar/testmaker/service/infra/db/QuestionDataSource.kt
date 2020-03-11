package jp.gr.java_conf.foobar.testmaker.service.infra.db

import io.realm.Realm
import jp.gr.java_conf.foobar.testmaker.service.domain.Quest
import jp.gr.java_conf.foobar.testmaker.service.domain.Question

class QuestionDataSource(private val realm: Realm) {

    fun create(question: Question): Long {
        val realmQuestion = Quest.createQuestFromQuestion(question)
        realmQuestion.id = realm.where(Quest::class.java).max("id")?.toLong()?.plus(1) ?: 0
        realmQuestion.order = realmQuestion.id.toInt()
        realm.executeTransaction {
            it.copyToRealm(realmQuestion)
        }
        return realmQuestion.id
    }

    fun get(): List<Question> = realm.copyFromRealm(realm.where(Quest::class.java)
            .findAll())
            ?.sortedBy { it.order }
            ?.map { Question.createFromRealmQuestion(it) }
            ?: listOf()

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