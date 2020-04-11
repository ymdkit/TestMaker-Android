package jp.gr.java_conf.foobar.testmaker.service.domain

import android.content.Context
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest

/**
 * Created by keita on 2017/02/08.
 */

open class RealmTest : RealmObject() {

    @PrimaryKey
    var id: Long = 0
    var color: Int = 0
    var limit: Int = 100
    var startPosition: Int = 0
    var title: String? = null
    private var category: String? = null
    var history: Long = 0
    private var questions: RealmList<Quest>? = null
    var documentId: String = ""
    var order: Int = 0

    fun setCategory(c: String) {
        category = c
    }

    fun getCategory(): String {
        return category ?: ""
    }

    fun addQuestion(q: Quest) {

        questions ?: run { questions = RealmList() }

        questions?.add(q)
    }

    fun questionsNonNull(): List<Quest> = questions?.sortedBy { it.order } ?: listOf()

    fun resetAchievement() {

        questions ?: return
        questions!!.forEach { it.correct = false }

    }

    fun toFirebaseTest(context: Context): FirebaseTest {

        var firebaseColor = 0

        context.resources.getIntArray(R.array.color_list).forEachIndexed { index, it ->
            if (color == it) firebaseColor = index
        }

        return FirebaseTest(name = title
                ?: "no title", color = firebaseColor)
    }

    companion object {
        fun createFromTest(test: Test): RealmTest {
            val realmTest = RealmTest()

            realmTest.id = test.id
            realmTest.color = test.color
            realmTest.limit = test.limit
            realmTest.startPosition = test.startPosition
            realmTest.title = test.title
            realmTest.setCategory(test.category)
            realmTest.history = test.history
            test.questions.forEach { realmTest.addQuestion(Quest.createQuestFromQuestion(it)) }
            realmTest.documentId = test.documentId
            realmTest.order = test.order

            return realmTest
        }
    }
}
