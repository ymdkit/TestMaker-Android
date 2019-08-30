package jp.gr.java_conf.foobar.testmaker.service.models

import android.content.Context
import android.util.Log
import android.widget.Toast
import io.realm.*
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.SharedPreferenceManager
import java.util.*

/**
 * Created by keita on 2017/02/08.
 */

class RealmController(context: Context, config: RealmConfiguration) {
    private val realm: Realm = Realm.getInstance(config)

    private val sharedPreferenceManager: SharedPreferenceManager = SharedPreferenceManager(context)

    fun getTest(testId: Long): Test {

        return realm.where(Test::class.java).equalTo("id", testId).findFirst() ?: Test()
    }

    fun addTest(title: String, color: Int, category: String): Long {

        realm.beginTransaction()

        // 初期化
        var nextUserId: Long = 1
        // userIdの最大値を取得
        val maxUserId = realm.where(Test::class.java).max("id")
        // 1度もデータが作成されていない場合はNULLが返ってくるため、NULLチェックをする
        if (maxUserId != null) {
            nextUserId = (maxUserId.toInt() + 1).toLong()
        }

        val test = realm.createObject(Test::class.java, nextUserId)

        test.title = title
        test.color = color
        test.setCategory(category)
        test.limit = 100

        realm.commitTransaction()

        return nextUserId
    }


    fun updateTest(test: Test, title: String, color: Int, category: String) {
        realm.beginTransaction()

        test.title = title
        test.color = color
        test.setCategory(category)

        realm.commitTransaction()
    }

    fun deleteTest(test: Test) {

        realm.beginTransaction()

        test.deleteFromRealm()

        realm.commitTransaction()
    }


    fun updateHistory(test: Test) {
        realm.beginTransaction()

        test.setHistory()

        realm.commitTransaction()
    }

    fun updateStart(test: Test, start: Int) {

        realm.beginTransaction()

        test.startPosition = start

        realm.commitTransaction()
    }

    fun updateLimit(test: Test, limit: Int) {

        realm.beginTransaction()

        test.limit = limit

        realm.commitTransaction()

    }

    fun updateCorrect(quest: Quest, correct: Boolean) {

        realm.beginTransaction()

        quest.correct = correct

        realm.commitTransaction()

    }

    fun updateSolving(questionId: Long, solving: Boolean) {
        realm.beginTransaction()

        val question = realm.where(Quest::class.java).equalTo("id", questionId).findFirst()
                ?: Quest()

        question.solving = solving

        realm.commitTransaction()
    }

    private fun updateOrder(questionId: Long, order: Int) {
        realm.beginTransaction()

        val question = realm.where(Quest::class.java).equalTo("id", questionId).findFirst()
                ?: Quest()

        question.order = order

        realm.commitTransaction()
    }


    fun close() {

        realm.close()

    }

    fun getCategorizedList(category: String): ArrayList<Test> {

        val array = ArrayList<Test>()

        val realmArray: RealmResults<Test>

        when (sharedPreferenceManager.sort) {
            -1 ->

                realmArray = realm.where(Test::class.java).findAll().sort("title")
            0 ->

                realmArray = realm.where(Test::class.java).findAll().sort("title")
            1 ->

                realmArray = realm.where(Test::class.java).findAll().sort("title", Sort.DESCENDING)
            2 ->

                realmArray = realm.where(Test::class.java).findAll().sort("history", Sort.DESCENDING)

            else -> realmArray = realm.where(Test::class.java).findAll().sort("title")
        }

        for (test in realmArray) {
            if (test.getCategory() == category) {
                array.add(test)
            }
        }

        return array

    }

    fun sortManual(from: Int, to: Int, testId: Long) {

        val questions = getTest(testId).questionsNonNull()

        val fromOrder = questions[from].order
        val toOrder = questions[to].order

        updateOrder(questions[from].id , toOrder)
        updateOrder(questions[to].id , fromOrder)

    }

    fun migrateOrder(testId: Long) {

        val questions = getTest(testId).questionsNonNull()

        if (questions.size < 2) return

        realm.beginTransaction()

        if (questions[0]?.order == questions[1]?.order) {

            getTest(testId).getQuestionsForEach().forEachIndexed { index, quest -> quest.order = index }

        }

        realm.commitTransaction()

    }

    fun resetSolving(testId: Long) {

        realm.beginTransaction()

        getTest(testId).getQuestionsForEach().forEach { it.solving = false }

        realm.commitTransaction()

    }

    fun resetAchievement(testId: Long) {

        realm.beginTransaction()

        getTest(testId).resetAchievement()

        realm.commitTransaction()


    }

    fun copyToRealm(it: Test) {
        realm.beginTransaction()

        if(it.id == 0L){
            it.id = realm.where(Test::class.java).max("id")?.toLong()?.plus(1) ?: -1L
        }

        realm.copyToRealmOrUpdate(it)
        realm.commitTransaction()

    }

    val maxQuestionId: Long
        get() {
            return realm.where(Quest::class.java).max("id")?.toLong() ?: 1
        }

}
