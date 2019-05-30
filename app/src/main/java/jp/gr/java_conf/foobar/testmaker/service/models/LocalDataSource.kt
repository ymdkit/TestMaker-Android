package jp.gr.java_conf.foobar.testmaker.service.models

import io.realm.Realm

class LocalDataSource(private val realm: Realm) {


    fun getTests(): List<Test>? {
        return emptyList()
    }

    fun getQuestions(testId: Long): ArrayList<Quest>? {

        val realmArray = getTest(testId).getQuestions()
        return ArrayList(realmArray)
    }

    private fun getTest(testId: Long): Test {
        return realm.where(Test::class.java).equalTo("id", testId).findFirst() ?: Test()

    }

    fun deleteQuestion(question: Quest) {
        realm.beginTransaction()
        question.deleteFromRealm()
        realm.commitTransaction()
    }

}