package jp.gr.java_conf.foobar.testmaker.service.domain

import android.content.Context
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import java.util.*
import kotlin.collections.ArrayList

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

    fun getQuestionsForEach(): RealmList<Quest> {//順番はどうでもいいが全てにアクセスしたい時

        return questions ?: RealmList()

    }

    fun setQuestions(q: RealmList<Quest>) {
        questions = q
    }

    fun testToString(context: Context, upload: Boolean): String {

        var backup = ""

        val questions = this.questions ?: return ""

        for (i in questions.indices) {
            val q = questions[i] ?: Quest()

            if (q.imagePath != "" && upload) continue //画像付き問題はスキップ

            var lineWrite = StringBuilder()

            when (q.type) {
                Constants.WRITE -> lineWrite = StringBuilder(context.getString(R.string.share_short_answers, q.problem.replace(",", "<comma>"), q.answer.replace(",", "<comma>")))

                Constants.COMPLETE -> {

                    lineWrite = StringBuilder(context.getString(if (q.isCheckOrder) R.string.share_multiple_answers_order else R.string.share_multiple_answers, q.problem.replace(",", "<comma>")))

                    for (k in 0 until q.answers.size) {
                        lineWrite.append(q.answers[k]!!.selection.replace(",", "<comma>")).append(",")
                    }
                    lineWrite = StringBuilder(lineWrite.substring(0, lineWrite.length - 1))
                }
                Constants.SELECT -> if (q.auto) {
                    lineWrite = StringBuilder(context.getString(R.string.share_selection_auto_problems, q.problem.replace(",", "<comma>"), q.answer.replace(",", "<comma>"), q.selections.size))

                } else {
                    lineWrite = StringBuilder(context.getString(R.string.share_selection_problems, q.problem.replace(",", "<comma>"), q.answer.replace(",", "<comma>")))

                    for (k in 0 until q.selections.size) {
                        lineWrite.append(q.selections[k]!!.selection.replace(",", "<comma>")).append(",")
                    }
                    lineWrite = StringBuilder(lineWrite.substring(0, lineWrite.length - 1))

                }

                Constants.SELECT_COMPLETE ->

                    if (q.auto) {
                        lineWrite = StringBuilder(context.getString(R.string.share_select_complete_auto_problem, q.problem.replace(",", "<comma>")))

                        lineWrite.append(q.selections.size.toString()).append(",")

                        for (k in 0 until q.answers.size) {
                            lineWrite.append(q.answers[k]!!.selection.replace(",", "<comma>")).append(",")
                        }

                        lineWrite = StringBuilder(lineWrite.substring(0, lineWrite.length - 1))

                    } else {
                        lineWrite = StringBuilder(context.getString(R.string.share_select_complete_problem, q.problem.replace(",", "<comma>")))

                        lineWrite.append(q.answers.size.toString()).append(",")

                        lineWrite.append(q.selections.size.toString()).append(",")

                        for (k in 0 until q.answers.size) {
                            lineWrite.append(q.answers[k]!!.selection.replace(",", "<comma>")).append(",")
                        }

                        for (k in 0 until q.selections.size) {
                            lineWrite.append(q.selections[k]!!.selection.replace(",", "<comma>")).append(",")
                        }
                        lineWrite = StringBuilder(lineWrite.substring(0, lineWrite.length - 1))

                    }
            }

            if (lineWrite.toString().contains("\n")) {
                lineWrite = StringBuilder(lineWrite.toString().replace("\n".toRegex(), "<br>"))
            }

            backup += lineWrite

            if (q.explanation != "") {
                backup += "\n"
                backup += context.getString(R.string.share_explanation, q.explanation).replace("\n".toRegex(), "<br>")
            }

            backup += "\n"

        }

        backup += context.getString(R.string.share_title, title)
        backup += "\n"
        backup += context.getString(R.string.share_category, getCategory())
        backup += "\n"
        backup += context.getString(R.string.share_color, color.toString())

        return backup

    }

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

    fun getQuestionsSolved(): ArrayList<Quest> {
        return ArrayList(questions?.filter { it.solving } ?: listOf())
    }

    fun getChoices(size: Int, answer: String, context: Context): ArrayList<String> {

        val result = arrayListOf<String>()

        for (q in questions?.take(100)?.shuffled() ?: emptyList()) {
            if (result.size >= size) break

            when (q.type) {
                Constants.WRITE, Constants.SELECT -> {
                    if (q.answer != answer) result.add(q.answer)
                }
                Constants.COMPLETE, Constants.SELECT_COMPLETE -> {
                    if (q.answers.isNotEmpty()) {
                        if (q.answers[0]?.selection != answer) result.add(q.answers[0]?.selection
                                ?: "")
                    }
                }
            }
        }

        while (result.size < size) {
            result.add(context.getString(R.string.message_not_auto))
        }

        return result
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
